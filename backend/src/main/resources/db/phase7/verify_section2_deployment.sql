-- ============================================================================
-- Section 2: Appointment Scheduling Integrity - Deployment Verification
-- ============================================================================

-- 1. Check triggers on appointments table
-- ============================================================================
-- === 1. TRIGGERS ON APPOINTMENTS TABLE ===
SELECT
    tgname as trigger_name,
    CASE WHEN tgdisabled = 0 THEN 'ENABLED' ELSE 'DISABLED' END as status
FROM pg_trigger
WHERE tgrelid = 'appointments'::regclass
ORDER BY tgname;

-- 2. Check trigger functions
-- ============================================================================
-- === 2. TRIGGER FUNCTIONS ===
SELECT
    proname as function_name,
    pronargs as parameter_count,
    prokind as kind
FROM pg_proc
WHERE proname IN ('trigger_appointments_enforce', 'trigger_appointments_no_overlap', 'is_valid_appointment_transition')
ORDER BY proname;

-- 3. Check background job procedure
-- ============================================================================
-- === 3. BACKGROUND JOB PROCEDURE ===
SELECT
    proname as procedure_name,
    prokind as kind,
    'PL/pgSQL' as language
FROM pg_proc
WHERE proname = 'job_mark_no_show';

-- 4. Check view
-- ============================================================================
-- === 4. VIEWS ===
SELECT
    viewname as view_name,
    schemaname as schema_name
FROM pg_views
WHERE viewname = 'v_overdue_appointments';

-- 5. Check appointment status constraint
-- ============================================================================
-- === 5. STATUS CONSTRAINT (CHECK) ===
SELECT
    constraint_name,
    constraint_type,
    table_name
FROM information_schema.table_constraints
WHERE table_name = 'appointments'
  AND constraint_type = 'CHECK'
  AND constraint_name LIKE '%status%';

-- 6. Verify constraint includes NO_SHOW
-- ============================================================================
-- === 6. CONSTRAINT DEFINITION ===
SELECT
    constraint_name,
    check_clause
FROM information_schema.check_constraints
WHERE constraint_name = 'appointments_status_chk';

-- 7. Test appointment status values
-- ============================================================================
-- === 7. VALID APPOINTMENT STATUSES ===
SELECT 'SCHEDULED' as status
UNION ALL
SELECT 'IN_PROGRESS'
UNION ALL
SELECT 'COMPLETED'
UNION ALL
SELECT 'CANCELLED'
UNION ALL
SELECT 'NO_SHOW'
ORDER BY status;

-- 8. Summary Statistics
-- ============================================================================
-- === 8. DEPLOYMENT SUMMARY ===
SELECT
    'Triggers' as component,
    COUNT(*) as count
FROM pg_trigger
WHERE tgrelid = 'appointments'::regclass
UNION ALL
SELECT
    'Trigger Functions' as component,
    COUNT(*) as count
FROM pg_proc
WHERE proname IN ('trigger_appointments_enforce', 'trigger_appointments_no_overlap')
UNION ALL
SELECT
    'Validation Functions' as component,
    COUNT(*) as count
FROM pg_proc
WHERE proname = 'is_valid_appointment_transition'
UNION ALL
SELECT
    'Background Procedures' as component,
    COUNT(*) as count
FROM pg_proc
WHERE proname = 'job_mark_no_show'
UNION ALL
SELECT
    'Views' as component,
    COUNT(*) as count
FROM pg_views
WHERE viewname = 'v_overdue_appointments'
ORDER BY component;

-- 9. Test the transition validation function
-- ============================================================================
-- === 9. STATUS TRANSITION VALIDATION TEST ===
SELECT
    'SCHEDULED → IN_PROGRESS' as transition,
    is_valid_appointment_transition('SCHEDULED', 'IN_PROGRESS') as valid
UNION ALL
SELECT 'SCHEDULED → COMPLETED', is_valid_appointment_transition('SCHEDULED', 'COMPLETED')
UNION ALL
SELECT 'SCHEDULED → CANCELLED', is_valid_appointment_transition('SCHEDULED', 'CANCELLED')
UNION ALL
SELECT 'IN_PROGRESS → COMPLETED', is_valid_appointment_transition('IN_PROGRESS', 'COMPLETED')
UNION ALL
SELECT 'COMPLETED → SCHEDULED', is_valid_appointment_transition('COMPLETED', 'SCHEDULED')
UNION ALL
SELECT 'COMPLETED → COMPLETED', is_valid_appointment_transition('COMPLETED', 'COMPLETED')
ORDER BY transition;
