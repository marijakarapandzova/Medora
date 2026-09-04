-- V3__Fix_LabTechnician_Admin_Structure.sql
-- Refactor LabTechnician and Admin to use user_id FK instead of duplicating user data

-- Step 1: Drop FK constraints from tables that reference lab_technician and admin
ALTER TABLE IF EXISTS billing DROP CONSTRAINT IF EXISTS "FKd17oy1jh5wl1m8vgitr17ks5m";
ALTER TABLE IF EXISTS performed_lab_tests DROP CONSTRAINT IF EXISTS "FK7itndt1cw4ekph05b0kuadfge";
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS "FKlsz5c3rwmg8f4p8xfay9wqy4w";
ALTER TABLE IF EXISTS users DROP CONSTRAINT IF EXISTS "FK9lxmsmidme9l8ofsx1xfyamtk";

-- Step 2: Clear admin_id and technician_id from billing and performed_lab_tests tables
UPDATE billing SET admin_id = NULL WHERE admin_id IS NOT NULL;
UPDATE performed_lab_tests SET technician_id = NULL WHERE technician_id IS NOT NULL;

-- Step 3: Create new structure for lab_technician table with user_id FK
CREATE TABLE IF NOT EXISTS lab_technician_new (
    technician_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    certification VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Step 4: Migrate existing lab_technician data
INSERT INTO lab_technician_new (technician_id, user_id, certification)
SELECT lt.technician_id, u.user_id, lt.email
FROM lab_technician lt
LEFT JOIN users u ON u.username = lt.username
WHERE u.user_id IS NOT NULL
ON CONFLICT DO NOTHING;

-- Step 5: Drop the old lab_technician table
DROP TABLE IF EXISTS lab_technician CASCADE;

-- Step 6: Rename new table to original name
ALTER TABLE lab_technician_new RENAME TO lab_technician;

-- Step 7: Create new structure for admin table with user_id FK
CREATE TABLE IF NOT EXISTS admin_new (
    admin_id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    permissions VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Step 8: Migrate existing admin data
INSERT INTO admin_new (admin_id, user_id, permissions)
SELECT a.admin_id, u.user_id, NULL
FROM admin a
LEFT JOIN users u ON u.username = a.username
WHERE u.user_id IS NOT NULL AND u.role = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Step 9: Drop the old admin table
DROP TABLE IF EXISTS admin CASCADE;

-- Step 10: Rename new table to original name
ALTER TABLE admin_new RENAME TO admin;

-- Step 11: Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_lab_technician_user_id ON lab_technician(user_id);
CREATE INDEX IF NOT EXISTS idx_admin_user_id ON admin(user_id);

-- Step 12: Add FK constraints back (now with proper data)
ALTER TABLE performed_lab_tests ADD CONSTRAINT FK7itndt1cw4ekph05b0kuadfge FOREIGN KEY (technician_id) REFERENCES lab_technician(technician_id) ON DELETE SET NULL;
