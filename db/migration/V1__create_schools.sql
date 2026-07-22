-- V1__create_schools.sql
-- Creates the schools table
-- This is the root table. All other tables reference schools(id).

CREATE TABLE schools (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(150)  NOT NULL,
    location    VARCHAR(200),
    level       VARCHAR(10)   NOT NULL CHECK (level IN ('JHS', 'SHS', 'BOTH')),
    created_at  TIMESTAMP     DEFAULT NOW()
);
