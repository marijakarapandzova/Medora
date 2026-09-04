-- Revenue reporting: monthly revenue, split by procedure vs. lab test.
-- Procedure revenue is attributed to a department, since `procedures` carries a
-- doctor_id directly on the catalog row. Lab revenue cannot be attributed to a
-- department: lab_tests has no doctor/department reference at all, and
-- billing_lab_tests links only to the catalog test_id, not to a specific performed
-- instance, so there is no reliable way to know which doctor/department administered
-- a billed test. Lab rows are reported at clinic-wide monthly granularity only
-- (department_id/department_name are NULL for those rows).

CREATE MATERIALIZED VIEW mv_revenue_monthly AS
SELECT
    DATE_TRUNC('month', b.payment_date)::DATE AS month,
    dept.department_id,
    dept.department_name,
    'PROCEDURE' AS revenue_type,
    SUM(p.cost) AS revenue,
    COUNT(DISTINCT b.bill_id) AS transaction_count
FROM billing b
         JOIN billing_procedures bp ON bp.bill_id = b.bill_id
         JOIN procedures p ON p.procedure_id = bp.procedure_id
         JOIN doctors doc ON doc.doctor_id = p.doctor_id
         JOIN departments dept ON dept.department_id = doc.department_id
WHERE b.payment_status = 'PAID'
GROUP BY DATE_TRUNC('month', b.payment_date), dept.department_id, dept.department_name

UNION ALL

SELECT
    DATE_TRUNC('month', b.payment_date)::DATE AS month,
    NULL AS department_id,
    NULL AS department_name,
    'LAB' AS revenue_type,
    SUM(lt.cost) AS revenue,
    COUNT(DISTINCT b.bill_id) AS transaction_count
FROM billing b
         JOIN billing_lab_tests blt ON blt.bill_id = b.bill_id
         JOIN lab_tests lt ON lt.test_id = blt.test_id
WHERE b.payment_status = 'PAID'
GROUP BY DATE_TRUNC('month', b.payment_date);

CREATE INDEX idx_mv_revenue_monthly_month ON mv_revenue_monthly (month, revenue_type);

-- Background job: recomputes the materialized view. Designed to be invoked
-- periodically (nightly) via pg_cron, OS-level cron, or an external scheduler.
-- pg_cron was not available in this hosting environment to test automatic
-- scheduling directly, so this procedure is verified by manual invocation:
--   CALL medora_job_refresh_revenue_view();
CREATE OR REPLACE PROCEDURE medora_job_refresh_revenue_view()
    LANGUAGE plpgsql
AS $$
BEGIN
    REFRESH MATERIALIZED VIEW mv_revenue_monthly;
END;
$$;

CREATE OR REPLACE VIEW v_current_month_revenue AS
SELECT month, department_id, department_name, revenue_type, revenue, transaction_count
FROM mv_revenue_monthly
WHERE month = DATE_TRUNC('month', CURRENT_DATE)::DATE
ORDER BY revenue_type, revenue DESC;