-- V4__create_biometrics.sql
-- Stores fingerprint biometric templates linked to each student.
-- template column holds the raw biometric template data (base64 or binary string).

CREATE TABLE biometric_templates (
    id          BIGSERIAL PRIMARY KEY,
    student_id  BIGINT    NOT NULL REFERENCES students(id),
    template    TEXT      NOT NULL,
    created_at  TIMESTAMP DEFAULT NOW()
);
