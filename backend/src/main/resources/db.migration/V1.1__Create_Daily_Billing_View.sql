-- Create materialized view for daily patient billing totals
CREATE MATERIALIZED VIEW IF NOT EXISTS daily_patient_billing_totals AS
SELECT
    p.patient_id,
    CAST(pp.procedure_date AS DATE) as service_date,
    COALESCE(SUM(pr.cost), 0) as procedure_cost,
    0::decimal as lab_test_cost,
    COALESCE(SUM(pr.cost), 0) as total_cost
FROM patients p
LEFT JOIN performed_procedures pp ON p.patient_id = pp.patient_id
LEFT JOIN procedures pr ON pp.procedure_id = pr.procedure_id
GROUP BY p.patient_id, CAST(pp.procedure_date AS DATE)

UNION ALL

SELECT
    p.patient_id,
    CAST(plt.test_date AS DATE) as service_date,
    0::decimal as procedure_cost,
    COALESCE(SUM(lt.cost), 0) as lab_test_cost,
    COALESCE(SUM(lt.cost), 0) as total_cost
FROM patients p
LEFT JOIN performed_lab_tests plt ON p.patient_id = plt.patient_id
LEFT JOIN lab_tests lt ON plt.test_id = lt.test_id
GROUP BY p.patient_id, CAST(plt.test_date AS DATE);

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_daily_billing_patient_date ON daily_patient_billing_totals(patient_id, service_date);