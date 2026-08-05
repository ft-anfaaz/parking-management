-- Parking Management System
-- Schema + seed data (MySQL 8+)

CREATE DATABASE IF NOT EXISTS parking_management;
USE parking_management;

-- ----------------------------------------------------------------------
-- Users (staff who log in to operate the system)
-- ----------------------------------------------------------------------
CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash CHAR(64)     NOT NULL,   -- SHA-256 hex digest
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('ADMIN', 'OPERATOR') NOT NULL DEFAULT 'OPERATOR',
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------
-- Customers
-- ----------------------------------------------------------------------
CREATE TABLE customers (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20)  NOT NULL UNIQUE,
    email       VARCHAR(100),
    address     VARCHAR(255),
    license_no  VARCHAR(50),
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------------------
-- Vehicles (each belongs to a customer)
-- ----------------------------------------------------------------------
CREATE TABLE vehicles (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    customer_id     INT NOT NULL,
    vehicle_number  VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type    ENUM('TWO_WHEELER', 'FOUR_WHEELER', 'HEAVY') NOT NULL,
    model           VARCHAR(100),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------
-- Vehicle brands and models (lookup lists for the Vehicles screen dropdowns;
-- grows over time as staff add "Other" brands/models). A brand belongs to one
-- vehicle_type, so the Brand dropdown can be filtered by the Type the user
-- picked - a few real brands (e.g. Honda) sell both scooters and cars, so
-- they appear as two separate catalog rows, one per type.
-- ----------------------------------------------------------------------
CREATE TABLE vehicle_brands (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    vehicle_type  ENUM('TWO_WHEELER', 'FOUR_WHEELER', 'HEAVY') NOT NULL,
    UNIQUE KEY uniq_brand_name_type (name, vehicle_type)
);

CREATE TABLE vehicle_models (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    brand_id  INT NOT NULL,
    name      VARCHAR(100) NOT NULL,
    UNIQUE KEY uniq_brand_model (brand_id, name),
    FOREIGN KEY (brand_id) REFERENCES vehicle_brands(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------
-- Hourly rates per vehicle type
-- ----------------------------------------------------------------------
CREATE TABLE rates (
    vehicle_type   ENUM('TWO_WHEELER', 'FOUR_WHEELER', 'HEAVY') PRIMARY KEY,
    rate_per_hour  DECIMAL(6,2) NOT NULL
);

-- ----------------------------------------------------------------------
-- Parking slots
-- ----------------------------------------------------------------------
CREATE TABLE parking_slots (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    slot_number  VARCHAR(10) NOT NULL UNIQUE,
    floor        INT NOT NULL DEFAULT 1,
    slot_type    ENUM('TWO_WHEELER', 'FOUR_WHEELER', 'HEAVY') NOT NULL,
    status       ENUM('AVAILABLE', 'OCCUPIED') NOT NULL DEFAULT 'AVAILABLE'
);

-- ----------------------------------------------------------------------
-- Bookings (one active booking per vehicle at a time)
-- ----------------------------------------------------------------------
CREATE TABLE bookings (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id  INT NOT NULL,
    slot_id     INT NOT NULL,
    entry_time  DATETIME NOT NULL,
    exit_time   DATETIME NULL,
    status      ENUM('ACTIVE', 'COMPLETED') NOT NULL DEFAULT 'ACTIVE',
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES parking_slots(id)
);

-- ----------------------------------------------------------------------
-- Payments (one per completed booking)
-- ----------------------------------------------------------------------
CREATE TABLE payments (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    booking_id     INT NOT NULL UNIQUE,
    hours_charged  INT NOT NULL,
    rate_per_hour  DECIMAL(6,2) NOT NULL,
    amount         DECIMAL(8,2) NOT NULL,
    payment_mode   ENUM('CASH', 'CARD', 'UPI') NOT NULL,
    payment_time   DATETIME NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE
);

-- ----------------------------------------------------------------------
-- Seed data
-- ----------------------------------------------------------------------

-- Default login: username "admin", password "admin123"
INSERT INTO users (username, password_hash, full_name, role) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'ADMIN');

INSERT INTO rates (vehicle_type, rate_per_hour) VALUES
('TWO_WHEELER', 10.00),
('FOUR_WHEELER', 30.00),
('HEAVY', 50.00);

-- 8 two-wheeler, 8 four-wheeler, 4 heavy-vehicle slots across 2 floors
INSERT INTO parking_slots (slot_number, floor, slot_type) VALUES
('A1', 1, 'TWO_WHEELER'), ('A2', 1, 'TWO_WHEELER'), ('A3', 1, 'TWO_WHEELER'), ('A4', 1, 'TWO_WHEELER'),
('A5', 2, 'TWO_WHEELER'), ('A6', 2, 'TWO_WHEELER'), ('A7', 2, 'TWO_WHEELER'), ('A8', 2, 'TWO_WHEELER'),
('B1', 1, 'FOUR_WHEELER'), ('B2', 1, 'FOUR_WHEELER'), ('B3', 1, 'FOUR_WHEELER'), ('B4', 1, 'FOUR_WHEELER'),
('B5', 2, 'FOUR_WHEELER'), ('B6', 2, 'FOUR_WHEELER'), ('B7', 2, 'FOUR_WHEELER'), ('B8', 2, 'FOUR_WHEELER'),
('C1', 1, 'HEAVY'), ('C2', 1, 'HEAVY'), ('C3', 2, 'HEAVY'), ('C4', 2, 'HEAVY');

-- Popular vehicle brands sold in India, one row per (brand, vehicle_type) -
-- a few brands (Honda) appear twice because they sell both two-wheelers and cars.
INSERT INTO vehicle_brands (id, name, vehicle_type) VALUES
-- Two-wheeler brands
(1, 'Hero MotoCorp', 'TWO_WHEELER'), (2, 'TVS', 'TWO_WHEELER'), (3, 'Bajaj', 'TWO_WHEELER'),
(4, 'Royal Enfield', 'TWO_WHEELER'), (5, 'Yamaha', 'TWO_WHEELER'), (6, 'Honda', 'TWO_WHEELER'),
(7, 'Suzuki', 'TWO_WHEELER'),
-- Four-wheeler brands
(8, 'Maruti Suzuki', 'FOUR_WHEELER'), (9, 'Hyundai', 'FOUR_WHEELER'), (10, 'Tata Motors', 'FOUR_WHEELER'),
(11, 'Mahindra', 'FOUR_WHEELER'), (12, 'Honda', 'FOUR_WHEELER'), (13, 'Toyota', 'FOUR_WHEELER'),
(14, 'Kia', 'FOUR_WHEELER'), (15, 'Renault', 'FOUR_WHEELER'), (16, 'Skoda', 'FOUR_WHEELER'),
(17, 'Volkswagen', 'FOUR_WHEELER'), (18, 'MG', 'FOUR_WHEELER'),
-- Heavy / commercial vehicle brands
(19, 'Ashok Leyland', 'HEAVY'), (20, 'Eicher', 'HEAVY'), (21, 'BharatBenz', 'HEAVY'),
(22, 'Piaggio', 'HEAVY');

INSERT INTO vehicle_models (brand_id, name) VALUES
-- Two-wheeler models
(1, 'Splendor'), (1, 'HF Deluxe'), (1, 'Passion Pro'), (1, 'Glamour'), (1, 'Xtreme'),
(2, 'Apache'), (2, 'Jupiter'), (2, 'Ntorq'), (2, 'Star City'),
(3, 'Pulsar'), (3, 'Platina'), (3, 'CT100'), (3, 'Chetak'), (3, 'Avenger'),
(4, 'Classic 350'), (4, 'Bullet 350'), (4, 'Hunter 350'), (4, 'Meteor 350'),
(5, 'FZ'), (5, 'R15'), (5, 'MT-15'), (5, 'Fascino'),
(6, 'Activa'), (6, 'Shine'), (6, 'Unicorn'), (6, 'SP125'),
(7, 'Access 125'), (7, 'Burgman'), (7, 'Gixxer'),
-- Four-wheeler models
(8, 'Swift'), (8, 'Baleno'), (8, 'WagonR'), (8, 'Alto'), (8, 'Dzire'), (8, 'Ertiga'), (8, 'Brezza'),
(9, 'Creta'), (9, 'i20'), (9, 'Venue'), (9, 'Verna'), (9, 'Grand i10 Nios'),
(10, 'Nexon'), (10, 'Punch'), (10, 'Tiago'), (10, 'Harrier'), (10, 'Safari'), (10, 'Altroz'),
(11, 'Scorpio'), (11, 'XUV700'), (11, 'Bolero'), (11, 'Thar'), (11, 'XUV300'),
(12, 'City'), (12, 'Amaze'), (12, 'WR-V'), (12, 'Elevate'),
(13, 'Innova Crysta'), (13, 'Fortuner'), (13, 'Glanza'), (13, 'Urban Cruiser'),
(14, 'Seltos'), (14, 'Sonet'), (14, 'Carens'),
(15, 'Kwid'), (15, 'Triber'), (15, 'Kiger'),
(16, 'Slavia'), (16, 'Kushaq'),
(17, 'Virtus'), (17, 'Taigun'),
(18, 'Hector'), (18, 'Astor'), (18, 'Comet'),
-- Heavy / commercial models
(19, 'Dost'), (19, 'Boss'), (19, 'Bada Dost'),
(20, 'Pro 2049'), (20, 'Pro 3015'), (20, 'Skyline Pro'),
(21, '914R'), (21, '1215C'), (21, '1917R'),
(22, 'Ape'), (22, 'Ape Xtra'), (22, 'Porter');
