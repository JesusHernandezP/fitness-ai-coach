CREATE TABLE users (
    id UUID NOT NULL,
    age INTEGER,
    activity_level VARCHAR(255),
    created_at TIMESTAMP(6) WITHOUT TIME ZONE,
    email VARCHAR(255) NOT NULL,
    height_cm DOUBLE PRECISION,
    name VARCHAR(255),
    password_hash VARCHAR(255),
    sex VARCHAR(255),
    weight_kg DOUBLE PRECISION,
    CONSTRAINT users_pkey PRIMARY KEY (id),
    CONSTRAINT uk6dotkott2kjsp8vw4d0m25fb7 UNIQUE (email),
    CONSTRAINT users_activity_level_check CHECK (activity_level IN ('SEDENTARY', 'LIGHT', 'MODERATE', 'ACTIVE', 'VERY_ACTIVE')),
    CONSTRAINT users_sex_check CHECK (sex IN ('MALE', 'FEMALE'))
);

CREATE TABLE exercises (
    id UUID NOT NULL,
    description VARCHAR(255),
    equipment VARCHAR(255),
    muscle_group VARCHAR(255),
    name VARCHAR(255) NOT NULL,
    CONSTRAINT exercises_pkey PRIMARY KEY (id)
);

CREATE TABLE foods (
    id UUID NOT NULL,
    calories DOUBLE PRECISION NOT NULL,
    carbs DOUBLE PRECISION NOT NULL,
    fat DOUBLE PRECISION NOT NULL,
    name VARCHAR(255) NOT NULL,
    protein DOUBLE PRECISION NOT NULL,
    CONSTRAINT foods_pkey PRIMARY KEY (id)
);

CREATE TABLE daily_logs (
    id UUID NOT NULL,
    calories_burned DOUBLE PRECISION NOT NULL,
    calories_consumed DOUBLE PRECISION NOT NULL,
    log_date DATE NOT NULL,
    steps INTEGER NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT daily_logs_pkey PRIMARY KEY (id),
    CONSTRAINT uk_daily_logs_user_date UNIQUE (user_id, log_date),
    CONSTRAINT fk71xen0j3tvxv8ywstl43wmlka FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE goals (
    id UUID NOT NULL,
    goal_type VARCHAR(255) NOT NULL,
    target_calories DOUBLE PRECISION NOT NULL,
    target_carbs DOUBLE PRECISION,
    target_fat DOUBLE PRECISION,
    target_protein DOUBLE PRECISION,
    target_weight DOUBLE PRECISION,
    user_id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT goals_pkey PRIMARY KEY (id),
    CONSTRAINT fkb1mp6ulyqkpcw6bc1a2mr7v1g FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT goals_goal_type_check CHECK (goal_type IN ('LOSE_WEIGHT', 'BUILD_MUSCLE', 'MAINTAIN'))
);

CREATE TABLE body_metrics (
    id UUID NOT NULL,
    date DATE NOT NULL,
    weight DOUBLE PRECISION NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT body_metrics_pkey PRIMARY KEY (id),
    CONSTRAINT unique_body_metrics_user_date UNIQUE (user_id, date),
    CONSTRAINT fk8qf3oxt7x5kgte04g2ea681cc FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE meals (
    id UUID NOT NULL,
    meal_type VARCHAR(255) NOT NULL,
    daily_log_id UUID NOT NULL,
    CONSTRAINT meals_pkey PRIMARY KEY (id),
    CONSTRAINT fk7hythh4dwviqql3m5qj6a9uyp FOREIGN KEY (daily_log_id) REFERENCES daily_logs (id),
    CONSTRAINT meals_meal_type_check CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'))
);

CREATE TABLE workout_sessions (
    id UUID NOT NULL,
    calories_burned DOUBLE PRECISION,
    duration INTEGER,
    reps INTEGER,
    sets INTEGER,
    daily_log_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    CONSTRAINT workout_sessions_pkey PRIMARY KEY (id),
    CONSTRAINT fkg7801uvfdd6eeu916kis9v5up FOREIGN KEY (daily_log_id) REFERENCES daily_logs (id),
    CONSTRAINT fk7pa7f8pm2vr2ji27q1uaj0dwg FOREIGN KEY (exercise_id) REFERENCES exercises (id)
);

CREATE TABLE meal_items (
    id UUID NOT NULL,
    calculated_calories DOUBLE PRECISION,
    quantity DOUBLE PRECISION NOT NULL,
    food_id UUID NOT NULL,
    meal_id UUID NOT NULL,
    CONSTRAINT meal_items_pkey PRIMARY KEY (id),
    CONSTRAINT fkchoxjvcjim16ipnv0se5w3uyu FOREIGN KEY (food_id) REFERENCES foods (id),
    CONSTRAINT fkbkra8m7kb523wvlyg55di7ecx FOREIGN KEY (meal_id) REFERENCES meals (id)
);

CREATE TABLE ai_recommendations (
    id UUID NOT NULL,
    advice TEXT NOT NULL,
    analysis_snapshot TEXT NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    daily_log_id UUID NOT NULL,
    model VARCHAR(255) NOT NULL,
    CONSTRAINT ai_recommendations_pkey PRIMARY KEY (id)
);

CREATE TABLE chat_sessions (
    id UUID NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    last_activity_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT chat_sessions_pkey PRIMARY KEY (id),
    CONSTRAINT fk_chat_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE chat_messages (
    id UUID NOT NULL,
    session_id UUID NOT NULL,
    role VARCHAR(255) NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT chat_messages_pkey PRIMARY KEY (id),
    CONSTRAINT fk_chat_messages_session FOREIGN KEY (session_id) REFERENCES chat_sessions (id) ON DELETE CASCADE,
    CONSTRAINT chat_messages_role_check CHECK (role IN ('USER', 'AI'))
);

CREATE INDEX idx_chat_sessions_user_last_activity
    ON chat_sessions (user_id, last_activity_at DESC);

CREATE INDEX idx_chat_messages_session_created_at
    ON chat_messages (session_id, created_at DESC);
