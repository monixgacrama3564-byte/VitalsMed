-- ═══════════════════════════════════════════════════════════
-- VitalsMed Database Schema — TriThread Sprint 1
-- Run this in phpMyAdmin or MySQL CLI on XAMPP
-- ═══════════════════════════════════════════════════════════

CREATE DATABASE IF NOT EXISTS vitalsmed CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE vitalsmed;

-- ───── USERS (with Role-Level Security) ─────
CREATE TABLE IF NOT EXISTS users (
    user_id     INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,   -- bcrypt hashed
    full_name   VARCHAR(100) NOT NULL,
    email       VARCHAR(100) UNIQUE,
    role        ENUM('admin', 'provider', 'patient') NOT NULL DEFAULT 'patient',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active   TINYINT(1) DEFAULT 1
);

-- ───── PATIENTS ─────
CREATE TABLE IF NOT EXISTS patients (
    patient_id      VARCHAR(10)  PRIMARY KEY,  -- e.g. PT001
    full_name       VARCHAR(100) NOT NULL,
    age             INT          NOT NULL,
    condition_name  VARCHAR(200) NOT NULL,
    provider_id     INT,   -- FK to users (role=provider) — Row-Level Security anchor
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_active       TINYINT(1) DEFAULT 1,
    FOREIGN KEY (provider_id) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ───── BIOMETRIC LOGS (Vitals) ─────
-- Implements: POST /vitals/log
CREATE TABLE IF NOT EXISTS biometric_logs (
    log_id          INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      VARCHAR(10) NOT NULL,
    heart_rate      DECIMAL(5,1),    -- bpm   | Safe: 60–100
    temperature     DECIMAL(4,1),    -- °C    | Safe: 36.1–37.5
    bp_systolic     INT,             -- mmHg  | Safe: 90–140
    bp_diastolic    INT,             -- mmHg  | Safe: 60–90
    hr_status       ENUM('Normal', 'Alert') GENERATED ALWAYS AS (
                        CASE WHEN heart_rate BETWEEN 60 AND 100 THEN 'Normal' ELSE 'Alert' END
                    ) STORED,
    temp_status     ENUM('Normal', 'Alert') GENERATED ALWAYS AS (
                        CASE WHEN temperature BETWEEN 36.1 AND 37.5 THEN 'Normal' ELSE 'Alert' END
                    ) STORED,
    logged_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    logged_by       INT,   -- FK to users (provider)
    FOREIGN KEY (patient_id) REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (logged_by)  REFERENCES users(user_id) ON DELETE SET NULL,
    -- Validation: reject values outside human-possible range
    CONSTRAINT chk_hr    CHECK (heart_rate   IS NULL OR (heart_rate   > 0 AND heart_rate   <= 300)),
    CONSTRAINT chk_temp  CHECK (temperature  IS NULL OR (temperature  >= 30 AND temperature <= 45)),
    CONSTRAINT chk_bpsys CHECK (bp_systolic  IS NULL OR (bp_systolic  > 0 AND bp_systolic  <= 300)),
    CONSTRAINT chk_bpdia CHECK (bp_diastolic IS NULL OR (bp_diastolic > 0 AND bp_diastolic <= 200))
);

-- ───── PRESCRIPTION SCHEDULES (Medications) ─────
-- Implements: GET /patient/medications | Adherence Windows
CREATE TABLE IF NOT EXISTS prescription_schedules (
    med_id          INT AUTO_INCREMENT PRIMARY KEY,
    patient_id      VARCHAR(10)  NOT NULL,
    medication_name VARCHAR(100) NOT NULL,
    dosage          VARCHAR(50)  NOT NULL,      -- e.g. "5mg", "500mg"
    daily_frequency INT          NOT NULL,      -- 1=once, 2=twice, 3=thrice, 4=four times
    first_dose_time TIME         NOT NULL,      -- e.g. 08:00:00
    next_dose_at    DATETIME,                   -- auto-calculated
    status          ENUM('Pending','Taken','Late','Missed') DEFAULT 'Pending',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME ON UPDATE CURRENT_TIMESTAMP,
    prescribed_by   INT,
    FOREIGN KEY (patient_id)   REFERENCES patients(patient_id) ON DELETE CASCADE,
    FOREIGN KEY (prescribed_by) REFERENCES users(user_id) ON DELETE SET NULL
);

-- ───── ADHERENCE LOG (Audit trail) ─────
CREATE TABLE IF NOT EXISTS adherence_log (
    adherence_id    INT AUTO_INCREMENT PRIMARY KEY,
    med_id          INT NOT NULL,
    patient_id      VARCHAR(10) NOT NULL,
    scheduled_at    DATETIME NOT NULL,
    confirmed_at    DATETIME,
    window_closes   DATETIME GENERATED ALWAYS AS (DATE_ADD(scheduled_at, INTERVAL 2 HOUR)) STORED,
    status          ENUM('Taken','Missed','Late') NOT NULL,
    logged_by       INT,
    FOREIGN KEY (med_id)      REFERENCES prescription_schedules(med_id) ON DELETE CASCADE,
    FOREIGN KEY (patient_id)  REFERENCES patients(patient_id) ON DELETE CASCADE
);

-- ───── SEED DATA ─────
INSERT IGNORE INTO users (username, password, full_name, email, role) VALUES
('admin',    '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Admin User', 'admin@vitalsmed.ph', 'admin'),
('drvelayo', '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'Dr. Apriliza Velayo', 'velayo@vitalsmed.ph', 'provider');

INSERT IGNORE INTO patients (patient_id, full_name, age, condition_name, provider_id) VALUES
('PT001', 'Maria Santos',   54, 'Hypertension',    2),
('PT002', 'Jose Reyes',     67, 'Diabetes Type 2',  2),
('PT003', 'Ana Dela Cruz',  42, 'Arrhythmia',       2);

INSERT IGNORE INTO prescription_schedules (patient_id, medication_name, dosage, daily_frequency, first_dose_time, next_dose_at, status, prescribed_by) VALUES
('PT001', 'Amlodipine', '5mg',   1, '08:00:00', DATE_ADD(NOW(), INTERVAL 2 HOUR), 'Pending', 2),
('PT001', 'Losartan',   '50mg',  2, '08:00:00', DATE_ADD(NOW(), INTERVAL 4 HOUR), 'Pending', 2),
('PT002', 'Metformin',  '500mg', 3, '07:00:00', DATE_SUB(NOW(), INTERVAL 3 HOUR), 'Late',    2);

-- ═══════════════════════════════════════════════════════════
-- ROW-LEVEL SECURITY VIEW
-- Providers can only see their own patients' data
-- ═══════════════════════════════════════════════════════════
CREATE OR REPLACE VIEW provider_patient_vitals AS
SELECT
    bl.log_id, bl.patient_id, p.full_name AS patient_name,
    bl.heart_rate, bl.temperature, bl.bp_systolic, bl.bp_diastolic,
    bl.hr_status, bl.temp_status, bl.logged_at,
    u.full_name AS provider_name
FROM biometric_logs bl
JOIN patients p ON bl.patient_id = p.patient_id
JOIN users u ON p.provider_id = u.user_id;

-- Index for performance
CREATE INDEX IF NOT EXISTS idx_vitals_patient ON biometric_logs(patient_id, logged_at DESC);
CREATE INDEX IF NOT EXISTS idx_meds_patient   ON prescription_schedules(patient_id, next_dose_at);
