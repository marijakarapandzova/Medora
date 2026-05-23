-- Create users table
CREATE TABLE IF NOT EXISTS users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    patient_id BIGINT,
    doctor_id BIGINT,
    is_active BOOLEAN DEFAULT true,
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)
);

-- Create index for username lookup
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Insert admin user (password: admin123)
INSERT INTO users (username, password, role, first_name, last_name, is_active)
VALUES ('admin', 'admin123', 'ADMIN', 'System', 'Administrator', true)
ON CONFLICT (username) DO NOTHING;

-- Insert sample patient users (password: password123 for all)
-- These will be linked to existing patients by EMBG
INSERT INTO users (username, password, role, first_name, last_name, patient_id, is_active)
SELECT p.embg, 'password123', 'PATIENT', p.first_name, p.last_name, p.patient_id, true
FROM patients p
WHERE NOT EXISTS (
    SELECT 1 FROM users u WHERE u.username = p.embg
)
LIMIT 39;
