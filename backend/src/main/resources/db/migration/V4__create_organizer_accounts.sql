ALTER TABLE events ADD COLUMN review_note VARCHAR(500);

CREATE TABLE organizer_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organizer_id BIGINT NOT NULL,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name VARCHAR(120) NOT NULL,
    password_change_required BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_organizer_users_username UNIQUE (username),
    CONSTRAINT fk_organizer_users_organizer FOREIGN KEY (organizer_id) REFERENCES organizers (id)
);

CREATE INDEX idx_organizer_users_organizer_id ON organizer_users (organizer_id);
