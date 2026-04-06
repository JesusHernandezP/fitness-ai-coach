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
