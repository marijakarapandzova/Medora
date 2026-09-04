-- schema addition: billing had no "issued" date, only payment_date (populated only once
-- paid), so there was no way to measure how long a still-PENDING bill has been outstanding.
-- Existing rows will backfill to NOW() at ALTER time, which is not historically accurate —
-- acceptable for this project, but worth noting.
ALTER TABLE billing ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT NOW();

CREATE TABLE IF NOT EXISTS billing_audit_log (
                                                 audit_id BIGSERIAL PRIMARY KEY,
                                                 bill_id BIGINT NOT NULL REFERENCES billing(bill_id),
    old_amount DECIMAL(12,2),
    new_amount DECIMAL(12,2),
    old_status TEXT,
    new_status TEXT,
    change_type TEXT CHECK (change_type IN ('INSERT', 'UPDATE', 'LINE_ITEM_ADD', 'LINE_ITEM_REMOVE')),
    changed_at TIMESTAMP DEFAULT NOW()
    );

-- Note: billing_procedures links to the catalog procedure_id, not to a specific
-- performed_procedures row, so when the same procedure type has been performed on more
-- than one patient there is no reliable way to verify a billed line item belongs to the
-- same patient as the bill. That check is intentionally left out rather than implemented
-- unreliably.

CREATE OR REPLACE FUNCTION recalculate_billing_total(p_bill_id BIGINT, p_change_type TEXT)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
v_procedure_total DECIMAL;
    v_lab_total DECIMAL;
    v_new_total DECIMAL;
BEGIN
SELECT COALESCE(SUM(p.cost), 0) INTO v_procedure_total
FROM billing_procedures bp
         JOIN procedures p ON p.procedure_id = bp.procedure_id
WHERE bp.bill_id = p_bill_id;

SELECT COALESCE(SUM(lt.cost), 0) INTO v_lab_total
FROM billing_lab_tests blt
         JOIN lab_tests lt ON lt.test_id = blt.test_id
WHERE blt.bill_id = p_bill_id;

v_new_total := v_procedure_total + v_lab_total;

UPDATE billing SET total_cost = v_new_total WHERE bill_id = p_bill_id;

INSERT INTO billing_audit_log (bill_id, new_amount, change_type)
VALUES (p_bill_id, v_new_total, p_change_type);
END;
$$;

-- one shared trigger function, reused for both billing_procedures and billing_lab_tests
-- (both tables have a bill_id column, so NEW.bill_id / OLD.bill_id works either way)
CREATE OR REPLACE FUNCTION t1_billing_line_item_changed()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        PERFORM recalculate_billing_total(OLD.bill_id, 'LINE_ITEM_REMOVE');
RETURN OLD;
ELSE
        PERFORM recalculate_billing_total(NEW.bill_id, 'LINE_ITEM_ADD');
RETURN NEW;
END IF;
END;
$$;

DROP TRIGGER IF EXISTS trg_billing_procedures_update_total ON billing_procedures;
CREATE TRIGGER trg_billing_procedures_update_total
    AFTER INSERT OR DELETE ON billing_procedures
FOR EACH ROW
EXECUTE FUNCTION t1_billing_line_item_changed();

DROP TRIGGER IF EXISTS trg_billing_lab_tests_update_total ON billing_lab_tests;
CREATE TRIGGER trg_billing_lab_tests_update_total
    AFTER INSERT OR DELETE ON billing_lab_tests
FOR EACH ROW
EXECUTE FUNCTION t1_billing_line_item_changed();

CREATE OR REPLACE FUNCTION is_valid_billing_transition(p_old TEXT, p_new TEXT)
RETURNS BOOLEAN
LANGUAGE sql
IMMUTABLE
AS $$
SELECT CASE
           WHEN p_old = p_new THEN TRUE
           WHEN p_old = 'PENDING' AND p_new IN ('PAID', 'CANCELLED') THEN TRUE
           ELSE FALSE
           END;
$$;

CREATE OR REPLACE FUNCTION t2_billing_status_transition()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT is_valid_billing_transition(OLD.payment_status, NEW.payment_status) THEN
        RAISE EXCEPTION 'Cannot transition billing status from % to %',
            OLD.payment_status, NEW.payment_status;
END IF;

    IF OLD.payment_status <> NEW.payment_status THEN
        INSERT INTO billing_audit_log (bill_id, old_status, new_status, change_type)
        VALUES (NEW.bill_id, OLD.payment_status, NEW.payment_status, 'UPDATE');
END IF;

RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_billing_status_transition ON billing;
CREATE TRIGGER trg_billing_status_transition
    BEFORE UPDATE ON billing
    FOR EACH ROW
    EXECUTE FUNCTION t2_billing_status_transition();

CREATE OR REPLACE VIEW v_overdue_billings AS
SELECT
    b.bill_id,
    p.patient_id, p.first_name, p.last_name,
    b.total_cost, b.payment_status, b.created_at,
    CURRENT_DATE - b.created_at::DATE AS days_outstanding,
        CASE
            WHEN CURRENT_DATE - b.created_at::DATE > 60 THEN 'CRITICAL'
        WHEN CURRENT_DATE - b.created_at::DATE > 30 THEN 'OVERDUE'
        ELSE 'PENDING'
END AS urgency
FROM billing b
JOIN medical_records mr ON b.record_id = mr.record_id
JOIN patients p ON mr.patient_id = p.patient_id
WHERE b.payment_status = 'PENDING'
  AND CURRENT_DATE - b.created_at::DATE >= 30
ORDER BY days_outstanding DESC;

CREATE OR REPLACE PROCEDURE job_billing_alerts()
LANGUAGE plpgsql
AS $$
DECLARE
    v_overdue_count INT;
BEGIN
    SELECT COUNT(*) INTO v_overdue_count FROM v_overdue_billings WHERE days_outstanding > 30;
    RAISE NOTICE 'Found % overdue billing records requiring follow-up', v_overdue_count;
END;
$$;