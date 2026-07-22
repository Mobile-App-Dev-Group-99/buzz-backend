-- V9__safety_schema_corrections.sql

ALTER TABLE exeats ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools(id);
ALTER TABLE notifications ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools(id);
