-- V2__create_users.sql
-- Creates the users table
-- Stores all system users: admins, teachers, parents.
-- password must be VARCHAR(255) — BCrypt hashes are 60 chars minimum.
-- NEVER store plain text passwords.

-- V2__create_users.sql
CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    school_id   BIGINT       NOT NULL REFERENCES schools(id),
    username    VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(30)  NOT NULL CHECK (role IN ('ADMIN', 'TEACHER', 'STUDENT', 'PARENT')),
    created_at  TIMESTAMP    DEFAULT NOW()
);