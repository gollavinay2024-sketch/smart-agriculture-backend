-- Smart Agriculture Database Schema
-- Compatible with MySQL 8.x and H2 (MySQL Mode)

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    phone_number VARCHAR(20),
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    preferred_language VARCHAR(10) DEFAULT 'en',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE TABLE IF NOT EXISTS farmers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    preferred_language VARCHAR(10) DEFAULT 'en',
    village_location VARCHAR(150),
    latitude DOUBLE,
    longitude DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assessments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    farmer_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    crop_type VARCHAR(50) NOT NULL,
    harvest_date DATE NOT NULL,
    harvest_time TIME NOT NULL,
    location_name VARCHAR(150) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    risk_score INT NOT NULL,
    precipitation_probability DOUBLE,
    rainfall_amount DOUBLE,
    temperature DOUBLE,
    relative_humidity DOUBLE,
    wind_speed DOUBLE,
    weather_condition VARCHAR(100),
    risk_explanation TEXT,
    recommendations TEXT,
    voice_alert_text TEXT,
    forecast_timestamp TIMESTAMP NULL,
    data_status VARCHAR(50) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_assessments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_assessments_user ON assessments(user_id);
CREATE INDEX IF NOT EXISTS idx_assessments_crop ON assessments(crop_type);
CREATE INDEX IF NOT EXISTS idx_assessments_risk ON assessments(risk_level);
CREATE INDEX IF NOT EXISTS idx_assessments_created ON assessments(created_at);
