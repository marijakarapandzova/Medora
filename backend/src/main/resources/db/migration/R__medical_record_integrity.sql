CREATE OR REPLACE FUNCTION t1_diagnosis_record_consistency()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_record_patient BIGINT;
    v_diagnosis_patient BIGINT;
BEGIN
    SELECT patient_id INTO v_record_patient FROM medical_records WHERE record_id = NEW.record_id;
    SELECT patient_id INTO v_diagnosis_patient FROM diagnosis WHERE diagnosis_id = NEW.diagnosis_id;

    IF v_record_patient IS NULL THEN
        RAISE EXCEPTION 'Medical record % not found', NEW.record_id;
    END IF;

    IF v_diagnosis_patient IS NULL THEN
        RAISE EXCEPTION 'Diagnosis % not found', NEW.diagnosis_id;
    END IF;

    IF v_record_patient <> v_diagnosis_patient THEN
        RAISE EXCEPTION 'Diagnosis % belongs to patient %, but medical record % belongs to patient %',
            NEW.diagnosis_id, v_diagnosis_patient, NEW.record_id, v_record_patient;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_diagnosis_record_consistency ON diagnosis_medical_records;
CREATE TRIGGER trg_diagnosis_record_consistency
    BEFORE INSERT ON diagnosis_medical_records
    FOR EACH ROW EXECUTE FUNCTION t1_diagnosis_record_consistency();

CREATE OR REPLACE FUNCTION t2_procedure_diagnosis_consistency()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_diagnosis_patient BIGINT;
BEGIN
    IF NEW.diagnosis_id IS NOT NULL THEN
        SELECT patient_id INTO v_diagnosis_patient FROM diagnosis WHERE diagnosis_id = NEW.diagnosis_id;

        IF v_diagnosis_patient IS NULL THEN
            RAISE EXCEPTION 'Diagnosis % not found', NEW.diagnosis_id;
        END IF;

        IF NEW.patient_id <> v_diagnosis_patient THEN
            RAISE EXCEPTION 'Procedure belongs to patient %, but diagnosis % belongs to patient %',
                NEW.patient_id, NEW.diagnosis_id, v_diagnosis_patient;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_procedure_diagnosis_consistency ON performed_procedures;
CREATE TRIGGER trg_procedure_diagnosis_consistency
    BEFORE INSERT OR UPDATE ON performed_procedures
                         FOR EACH ROW EXECUTE FUNCTION t2_procedure_diagnosis_consistency();

CREATE OR REPLACE FUNCTION t3_referral_consistency()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM medical_records WHERE record_id = NEW.record_id) THEN
        RAISE EXCEPTION 'Medical record % not found', NEW.record_id;
    END IF;

    IF NEW.from_doctor_id = NEW.to_doctor_id THEN
        RAISE EXCEPTION 'Doctor % cannot refer to themselves', NEW.from_doctor_id;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_referral_consistency ON referrals;
CREATE TRIGGER trg_referral_consistency
    BEFORE INSERT ON referrals
    FOR EACH ROW EXECUTE FUNCTION t3_referral_consistency();

CREATE OR REPLACE VIEW v_medical_record_overview AS
SELECT
    mr.record_id,
    p.patient_id, p.first_name, p.last_name, p.embg,
    COUNT(DISTINCT dmr.diagnosis_id) AS diagnosis_count,
    COUNT(DISTINCT CASE WHEN d.patient_id IS NOT NULL AND d.patient_id <> p.patient_id THEN dmr.diagnosis_id END) AS diagnosis_mismatches,
    COUNT(DISTINCT mrp.procedure_id) AS procedures_count,
    COUNT(DISTINCT mrl.result_id) AS lab_results_count,
    COUNT(DISTINCT ref.referral_id) AS referrals_count,
    COUNT(DISTINCT mra.allergy_id) AS allergies_count,
    COUNT(DISTINCT CASE WHEN ref.from_doctor_id = ref.to_doctor_id THEN ref.referral_id END) AS self_referrals_detected
FROM medical_records mr
    JOIN patients p ON mr.patient_id = p.patient_id
    LEFT JOIN diagnosis_medical_records dmr ON dmr.record_id = mr.record_id
    LEFT JOIN diagnosis d ON d.diagnosis_id = dmr.diagnosis_id
    LEFT JOIN medical_record_procedures mrp ON mrp.record_id = mr.record_id
    LEFT JOIN medical_record_lab_results mrl ON mrl.record_id = mr.record_id
    LEFT JOIN referrals ref ON mr.record_id = ref.record_id
    LEFT JOIN medical_record_allergies mra ON mr.record_id = mra.record_id
GROUP BY mr.record_id, p.patient_id, p.first_name, p.last_name, p.embg;