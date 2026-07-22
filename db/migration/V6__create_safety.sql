-- V6__create_safety.sql
-- exeats: tracks approved early exits from campus
-- notifications: push/SMS notifications sent to parents

-- approved_by references users(id) so we know exactly which teacher approved
-- expected_return: when the student is supposed to be back
-- created_at: when the exeat request was submitted

CREATE TABLE exeats (
    id              BIGSERIAL    PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id),
    approved_by     BIGINT       REFERENCES users(id),
    reason          VARCHAR(255),
    expected_return TIMESTAMP,
    actual_return   TIMESTAMP,
    created_at      TIMESTAMP    DEFAULT NOW(),
    status          VARCHAR(20)  DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'RETURNED'))
);

-- Notifications sent to parents for arrivals, departures, exeats, and absences
CREATE TABLE notifications (
    id          BIGSERIAL    PRIMARY KEY,
    parent_id   BIGINT       NOT NULL REFERENCES parents(id),
    message     VARCHAR(255) NOT NULL,
    type        VARCHAR(50),
    sent_at     TIMESTAMP    DEFAULT NOW(),
    is_read     BOOLEAN      DEFAULT FALSE
);
