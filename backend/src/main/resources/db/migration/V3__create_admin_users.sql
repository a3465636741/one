CREATE TABLE admin_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    role VARCHAR(40) NOT NULL,
    password_change_required BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_admin_users_username UNIQUE (username)
);
