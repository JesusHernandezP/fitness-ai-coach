-- The legacy dev volume contains two equivalent unique constraints on daily_logs(user_id, log_date):
-- one created by Hibernate and one by V027. Keep the canonical constraint name only.
ALTER TABLE daily_logs
DROP CONSTRAINT IF EXISTS unique_user_date;
