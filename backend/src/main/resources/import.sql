-- Add appointment_date and appointment_time columns to referrals table if they don't exist
ALTER TABLE IF EXISTS referrals ADD COLUMN IF NOT EXISTS appointment_date DATE;
ALTER TABLE IF EXISTS referrals ADD COLUMN IF NOT EXISTS appointment_time TIME;