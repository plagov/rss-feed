CREATE TABLE IF NOT EXISTS tokens
(
    id          SERIAL PRIMARY KEY,
    token       VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP,
    description VARCHAR(255) NOT NULL
);
