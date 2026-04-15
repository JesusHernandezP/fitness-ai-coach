ALTER TABLE daily_logs
ADD CONSTRAINT uk_daily_logs_user_log_date UNIQUE (user_id, log_date);

ALTER TABLE ai_recommendations
ADD CONSTRAINT fk_ai_recommendations_daily_log
FOREIGN KEY (daily_log_id) REFERENCES daily_logs (id);
