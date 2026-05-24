-- Insert Billing Admin users
INSERT INTO users (username, password, role, first_name, last_name, is_active)
SELECT 'admin_ilija', 'adminmedora123', 'BILLING_ADMIN', 'Ilija', 'Admin', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin_ilija')
UNION ALL
SELECT 'admin_elena', 'adminmedora123', 'BILLING_ADMIN', 'Elena', 'Admin', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin_elena')
UNION ALL
SELECT 'admin_marjan', 'adminmedora123', 'BILLING_ADMIN', 'Marjan', 'Admin', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin_marjan')
UNION ALL
SELECT 'admin_vesna', 'adminmedora123', 'BILLING_ADMIN', 'Vesna', 'Admin', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin_vesna')
UNION ALL
SELECT 'admin_dushanka', 'adminmedora123', 'BILLING_ADMIN', 'Dushanka', 'Admin', true
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin_dushanka');
