-- schema change: NO_SHOW must be added to the existing status constraint before
-- anything below can ever set it
ALTER TABLE appointments DROP CONSTRAINT appointments_status_chk;
ALTER TABLE appointments ADD CONSTRAINT appointments_status_chk
    CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED','IN_PROGRESS','NO_SHOW'));

-- No generated column needed - we'll compute the time ranges directly in the triggers
-- This approach is simpler and avoids immutability constraints

CREATE OR REPLACE FUNCTION is_valid_appointment_transition(p_old TEXT, p_new TEXT)
RETURNS BOOLEAN
LANGUAGE sql
IMMUTABLE
AS $$
SELECT CASE
           WHEN p_old = p_new THEN TRUE
           WHEN p_old = 'SCHEDULED'    AND p_new IN ('IN_PROGRESS', 'COMPLETED', 'CANCELLED') THEN TRUE
           WHEN p_old = 'IN_PROGRESS'  AND p_new IN ('COMPLETED', 'CANCELLED') THEN TRUE
           ELSE FALSE
           END;
$$;

CREATE OR REPLACE FUNCTION trigger_appointments_enforce()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_combined_datetime TIMESTAMP;
BEGIN
    v_combined_datetime := NEW.appointment_date::TIMESTAMP + NEW.appointment_time;

    IF TG_OP = 'INSERT' AND v_combined_datetime < NOW() THEN
        RAISE EXCEPTION 'Cannot schedule appointment in the past (appointment_date=%, appointment_time=%)',
            NEW.appointment_date, NEW.appointment_time;
    END IF;

    IF TG_OP = 'UPDATE' THEN
        IF NOT is_valid_appointment_transition(OLD.status, NEW.status) THEN
            RAISE EXCEPTION 'Appointment status cannot transition from % to %',
                OLD.status, NEW.status;
        END IF;

        IF NEW.status = 'COMPLETED' AND v_combined_datetime > NOW() THEN
            RAISE EXCEPTION 'Cannot mark appointment COMPLETED before its scheduled time (scheduled for %)',
                v_combined_datetime;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trigger_appointments_enforce ON appointments;
CREATE TRIGGER trigger_appointments_enforce
    BEFORE INSERT OR UPDATE
                         ON appointments
                         FOR EACH ROW
                         EXECUTE FUNCTION trigger_appointments_enforce();

CREATE OR REPLACE FUNCTION t1_appointments_no_overlap()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_new_start TIMESTAMP;
    v_new_end TIMESTAMP;
BEGIN
    IF NEW.status NOT IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED') THEN
        RETURN NEW;
    END IF;

    v_new_start := NEW.appointment_date::TIMESTAMP + NEW.appointment_time;
    v_new_end := v_new_start + INTERVAL '30 minutes';

    -- Check for doctor double-booking
    -- Overlap condition: existing_start < new_end AND new_start < existing_end
    IF EXISTS (
        SELECT 1 FROM appointments a
        WHERE a.doctor_id = NEW.doctor_id
          AND a.status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED')
          AND (a.appointment_date::TIMESTAMP + a.appointment_time) < v_new_end
          AND v_new_start < (a.appointment_date::TIMESTAMP + a.appointment_time + INTERVAL '30 minutes')
          AND (TG_OP <> 'UPDATE' OR a.appointment_id <> NEW.appointment_id)
    ) THEN
        RAISE EXCEPTION 'Doctor % has overlapping appointment at %', NEW.doctor_id, NEW.appointment_date;
    END IF;

    -- Check for patient double-booking
    IF EXISTS (
        SELECT 1 FROM appointments a
        WHERE a.patient_id = NEW.patient_id
          AND a.status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED')
          AND (a.appointment_date::TIMESTAMP + a.appointment_time) < v_new_end
          AND v_new_start < (a.appointment_date::TIMESTAMP + a.appointment_time + INTERVAL '30 minutes')
          AND (TG_OP <> 'UPDATE' OR a.appointment_id <> NEW.appointment_id)
    ) THEN
        RAISE EXCEPTION 'Patient % has overlapping appointment at %', NEW.patient_id, NEW.appointment_date;
    END IF;

    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trigger_appointments_no_overlap ON appointments;
CREATE TRIGGER trigger_appointments_no_overlap
    BEFORE INSERT OR UPDATE
                         ON appointments
                         FOR EACH ROW
                         EXECUTE FUNCTION t1_appointments_no_overlap();

CREATE OR REPLACE PROCEDURE job_mark_no_show()
LANGUAGE plpgsql
AS $$
BEGIN
    UPDATE appointments
    SET status = 'NO_SHOW'
    WHERE status = 'SCHEDULED'
      AND (appointment_date::TIMESTAMP + appointment_time) < (NOW() - INTERVAL '45 minutes');
END;
$$;

CREATE OR REPLACE VIEW v_overdue_appointments AS
SELECT
    a.appointment_id, a.patient_id, p.first_name, p.last_name,
    a.doctor_id, d.first_name AS doctor_first_name, d.last_name AS doctor_last_name,
    a.appointment_date, a.appointment_time, a.status,
    NOW() - (a.appointment_date::TIMESTAMP + a.appointment_time) AS time_overdue
FROM appointments a
         JOIN patients p ON a.patient_id = p.patient_id
         JOIN doctors d ON a.doctor_id = d.doctor_id
WHERE a.status = 'SCHEDULED'
  AND (a.appointment_date::TIMESTAMP + a.appointment_time) < (NOW() - INTERVAL '45 minutes')
ORDER BY time_overdue DESC;