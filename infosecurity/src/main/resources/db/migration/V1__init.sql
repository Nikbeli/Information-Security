CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(512),
    role VARCHAR(50) NOT NULL,
    account_locked BOOLEAN NOT NULL,
    password_restrictions BOOLEAN NOT NULL,
    password_expiration_months INT NOT NULL,
    password_last_changed TIMESTAMP,
    min_password_length INT,
    failed_attempts INT NOT NULL,
    first_login BOOLEAN,
    email_confirmed BOOLEAN NOT NULL
);
