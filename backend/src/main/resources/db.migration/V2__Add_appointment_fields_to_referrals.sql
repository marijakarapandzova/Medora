-- Add appointment_date and appointment_time columns to referrals table
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS appointment_date DATE;
ALTER TABLE referrals ADD COLUMN IF NOT EXISTS appointment_time TIME;

-- Set default values for existing rows
UPDATE referrals SET appointment_date = referral_date + INTERVAL '1 day', appointment_time = '10:00:00'
WHERE appointment_date IS NULL;