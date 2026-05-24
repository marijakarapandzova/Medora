-- Insert Doctor users from doctors table
INSERT INTO users (username, password, role, first_name, last_name, doctor_id, is_active)
SELECT d.email_address, 'doctor123', 'DOCTOR', d.first_name, d.last_name, d.doctor_id, true
FROM doctors d
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.username = d.email_address
)
ON CONFLICT (username) DO NOTHING;
