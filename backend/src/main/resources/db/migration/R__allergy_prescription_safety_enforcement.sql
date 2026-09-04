CREATE DOMAIN non_negative_currency AS DECIMAL(12,2)
    CHECK (VALUE >= 0);

CREATE OR REPLACE FUNCTION t1_prescription_allergy_check()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_patient_id BIGINT;
    v_conflict_allergy_id BIGINT;
    v_allergy_name TEXT;
BEGIN
    SELECT patient_id INTO v_patient_id
    FROM medical_records
    WHERE record_id = NEW.record_id;

    IF v_patient_id IS NULL THEN
        RAISE EXCEPTION 'Medical record % not found', NEW.record_id;
    END IF;

    SELECT apr.allergy_id, a.name
    INTO v_conflict_allergy_id, v_allergy_name
    FROM prescription_restriction pr_rest
    JOIN allergy_prescription_restrictions apr ON apr.restriction_id = pr_rest.restriction_id
    JOIN allergies a ON apr.allergy_id = a.allergy_id
    JOIN medical_record_allergies mra ON a.allergy_id = mra.allergy_id
    WHERE pr_rest.prescription_id = NEW.prescription_id
    AND mra.record_id = NEW.record_id
    LIMIT 1;

    IF v_conflict_allergy_id IS NOT NULL THEN
        RAISE EXCEPTION 'PRESCRIPTION_ALLERGY_CONFLICT: Prescription % conflicts with allergy % (%) in patient''s record %',
            NEW.prescription_id, v_conflict_allergy_id, v_allergy_name, NEW.record_id;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_prescription_allergy_check ON prescription_medical_records;
CREATE TRIGGER trg_prescription_allergy_check
    BEFORE INSERT
    ON prescription_medical_records
    FOR EACH ROW
    EXECUTE FUNCTION t1_prescription_allergy_check();

CREATE TABLE IF NOT EXISTS prescription_allergy_conflicts_log (
    log_id BIGSERIAL PRIMARY KEY,
    record_id BIGINT NOT NULL REFERENCES medical_records(record_id),
    prescription_id BIGINT NOT NULL REFERENCES prescriptions(prescription_id),
    allergy_id BIGINT NOT NULL REFERENCES allergies(allergy_id),
    conflict_type TEXT CHECK (conflict_type IN ('ACTIVE', 'RESOLVED')),
    detected_date TIMESTAMP DEFAULT NOW(),
    resolution_notes TEXT,
    resolved_date TIMESTAMP
);

CREATE OR REPLACE VIEW v_prescription_allergy_conflicts AS
SELECT
    mr.record_id,
    p.patient_id,
    p.first_name,
    p.last_name,
    pmr.prescription_id,
    pr.medication_name,
    a.allergy_id,
    a.name AS allergy_name,
    a.allergy_severity,
    apr.restriction_id,
    pr_rest.description AS restriction_description,
    CASE
        WHEN mra.record_id IS NOT NULL THEN 'ACTIVE_CONFLICT'
        ELSE 'ARCHIVED'
    END AS conflict_status
FROM prescription_medical_records pmr
JOIN medical_records mr ON pmr.record_id = mr.record_id
JOIN patients p ON mr.patient_id = p.patient_id
JOIN prescriptions pr ON pmr.prescription_id = pr.prescription_id
JOIN prescription_restriction pr_rest ON pr.prescription_id = pr_rest.prescription_id
JOIN allergy_prescription_restrictions apr ON pr_rest.restriction_id = apr.restriction_id
JOIN allergies a ON apr.allergy_id = a.allergy_id
LEFT JOIN medical_record_allergies mra ON mr.record_id = mra.record_id
    AND a.allergy_id = mra.allergy_id
WHERE a.allergy_severity IN ('HIGH', 'CRITICAL')
ORDER BY p.patient_id, a.allergy_severity DESC;