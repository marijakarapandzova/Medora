-- Create scoped application role with limited privileges
-- Run this as the postgres superuser once, then update application.properties
-- to connect as medora_app instead of postgres

-- Step 1: Create the scoped application user
-- WARNING: Change the password to something strong and unique before running!
-- This example uses a placeholder — use: openssl rand -base64 32
-- DO NOT commit the actual password to source control
CREATE USER medora_app WITH PASSWORD 'CHANGE_ME_TO_A_STRONG_PASSWORD';

-- Step 2: Grant connection and usage privileges
GRANT CONNECT ON DATABASE medora TO medora_app;
GRANT USAGE ON SCHEMA public TO medora_app;

-- Step 3: Grant data manipulation privileges (CRUD only, no DDL)
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO medora_app;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO medora_app;

-- Step 4: Ensure future tables (created by migrations) automatically grant permissions
ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO medora_app;

ALTER DEFAULT PRIVILEGES IN SCHEMA public
GRANT USAGE ON SEQUENCES TO medora_app;

-- Verification queries (run as medora_app to confirm access):
-- SELECT * FROM patients LIMIT 1;  -- should work
-- CREATE TABLE test (id INT);       -- should fail (not permitted)
-- DROP TABLE patients;              -- should fail (not permitted)

-- After confirming this works:
-- 1. Update application.properties: spring.datasource.username=medora_app
-- 2. Update application.properties: spring.datasource.password=${DB_PASSWORD}
-- 3. Set environment variable: export DB_PASSWORD='<the password you chose>'
-- 4. Restart the application
