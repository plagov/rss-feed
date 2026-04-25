CREATE TABLE IF NOT EXISTS users
(
    id            UUID PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email         VARCHAR(255),
    created_at    TIMESTAMP    NOT NULL
);

ALTER TABLE blogs
ADD COLUMN IF NOT EXISTS user_id UUID;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_blogs_user_id'
    ) THEN
        ALTER TABLE blogs
            ADD CONSTRAINT fk_blogs_user_id
                FOREIGN KEY (user_id) REFERENCES users (id);
    END IF;
END $$;
