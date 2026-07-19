CREATE TABLE organizers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    contact VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_organizers_name UNIQUE (name)
);

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    slug VARCHAR(80) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    category_id BIGINT NOT NULL,
    organizer_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    location VARCHAR(200) NOT NULL,
    cover_url VARCHAR(500),
    source_url VARCHAR(500),
    status VARCHAR(32) NOT NULL,
    demo BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT fk_events_organizer FOREIGN KEY (organizer_id) REFERENCES organizers (id)
);

CREATE TABLE event_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    type VARCHAR(32) NOT NULL,
    label VARCHAR(100) NOT NULL,
    url VARCHAR(500),
    qr_image_url VARCHAR(500),
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_event_entries_event FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);

CREATE INDEX idx_events_status_start_time ON events (status, start_time);
CREATE INDEX idx_events_category_id ON events (category_id);
CREATE INDEX idx_event_entries_event_id ON event_entries (event_id);
