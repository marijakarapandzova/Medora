-- Insert Lab Technician users
INSERT INTO users (username, password, role, first_name, last_name, is_active)
VALUES
  ('lab_darko', 'lab123', 'LAB_TECHNICIAN', 'Darko', 'Milosev', true),
  ('lab_biljana', 'lab123', 'LAB_TECHNICIAN', 'Biljana', 'Trajkovska', true),
  ('lab_stefan', 'lab123', 'LAB_TECHNICIAN', 'Stefan', 'Nikolovski', true),
  ('lab_marina', 'lab123', 'LAB_TECHNICIAN', 'Marina', 'Petreska', true),
  ('lab_aleksandar', 'lab123', 'LAB_TECHNICIAN', 'Aleksandar', 'Ristovski', true)
ON CONFLICT (username) DO NOTHING;