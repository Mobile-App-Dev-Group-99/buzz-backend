-- V5__create_attendance.sql
-- Records every biometric scan event at the school gate.
-- scan_type: ARRIVAL or DEPARTURE
-- is_late: set to TRUE by the attendance-service if student arrives after cutoff time
-- school_id added for multi-tenancy filtering across services

-- V5__create_attendance.sql
CREATE TABLE attendance_events (
    id          BIGSERIAL   PRIMARY KEY,
    student_id  BIGINT      NOT NULL REFERENCES students(id),
    school_id   BIGINT      NOT NULL REFERENCES schools(id),
    scan_type   VARCHAR(20) NOT NULL CHECK (scan_type IN ('ARRIVAL', 'DEPARTURE')),
    scanned_at  TIMESTAMP   NOT NULL,
    is_late     BOOLEAN     DEFAULT FALSE,
    created_at  TIMESTAMP   DEFAULT NOW()
);