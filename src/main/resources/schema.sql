CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    enabled BOOLEAN DEFAULT FALSE,
    email_verified BOOLEAN DEFAULT FALSE
);

CREATE TABLE verification_tokens (
    id SERIAL PRIMARY KEY,
    token VARCHAR(255),
    user_id INTEGER NOT NULL REFERENCES users(id),
    expiry_date TIMESTAMP,
    verified BOOLEAN DEFAULT FALSE
); 