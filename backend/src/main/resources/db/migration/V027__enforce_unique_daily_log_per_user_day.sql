-- Migration: remove duplicate daily logs and enforce one log per user per day.
-- Date: 2026-03-31

DELETE FROM daily_logs dl
USING (
    SELECT id
    FROM (
        SELECT id,
               ROW_NUMBER() OVER (PARTITION BY user_id, log_date ORDER BY id) AS row_num
        FROM daily_logs
    ) duplicated_logs
    WHERE duplicated_logs.row_num > 1
) duplicates
WHERE dl.id = duplicates.id;

ALTER TABLE daily_logs
ADD CONSTRAINT unique_user_date UNIQUE (user_id, log_date);
