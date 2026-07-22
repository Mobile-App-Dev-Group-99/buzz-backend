-- V3__create_students.sql
-- Creates students, parents, and the students_parents junction table.
-- A student can have more than one parent/guardian, hence the many-to-many junction table.

CREATE TABLE students (
    id              BIGSERIAL    PRIMARY KEY,
    school_id       BIGINT       NOT NULL REFERENCES schools(id),
    user_id         BIGINT       REFERENCES users(id),
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    date_of_birth   DATE,
    gender          VARCHAR(10),
    student_type    VARCHAR(20),
    photo_url       VARCHAR(300),
    created_at      TIMESTAMP    DEFAULT NOW()
);

CREATE TABLE parents (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       REFERENCES users(id),
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    phone       VARCHAR(20),
    created_at  TIMESTAMP    DEFAULT NOW()
);

-- Junction table: links students to their parents/guardians
CREATE TABLE students_parents (
    student_id  BIGINT NOT NULL REFERENCES students(id),
    parent_id   BIGINT NOT NULL REFERENCES parents(id),
    PRIMARY KEY (student_id, parent_id)
);
