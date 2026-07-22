-- V7__create_academic.sql
-- Stores academic results per student per subject per term.
-- submitted_by references users(id) — must be a teacher user, not just a name string.
-- teacher_remark: free-text personal remark shown on the result UI.
-- year: the academic year the result belongs to (e.g. 2025).

CREATE TABLE academic_results (
    id              BIGSERIAL    PRIMARY KEY,
    student_id      BIGINT       NOT NULL REFERENCES students(id),
    submitted_by    BIGINT       NOT NULL REFERENCES users(id),
    subject         VARCHAR(100) NOT NULL,
    score           NUMERIC(5,2),
    grade           VARCHAR(5),
    term            VARCHAR(20),
    year            INT,
    teacher_remark  TEXT,
    created_at      TIMESTAMP    DEFAULT NOW()
);
