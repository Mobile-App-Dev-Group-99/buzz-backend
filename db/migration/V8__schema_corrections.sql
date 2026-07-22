-- V8__schema_corrections.sql

-- attendance_events: needs school_id for tenant scoping, plus gate + status
ALTER TABLE attendance_events ADD COLUMN IF NOT EXISTS school_id BIGINT REFERENCES schools(id);
ALTER TABLE attendance_events ADD COLUMN IF NOT EXISTS gate VARCHAR(50);
ALTER TABLE attendance_events ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- parents: needs email, first_name, last_name, and a link to their user account
ALTER TABLE parents ADD COLUMN IF NOT EXISTS email VARCHAR(255);
ALTER TABLE parents ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE parents ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE parents ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);

-- students: needs first/last name split, DOB, gender, student_type, photo, user link, class_name
ALTER TABLE students ADD COLUMN IF NOT EXISTS first_name VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS last_name VARCHAR(100);
ALTER TABLE students ADD COLUMN IF NOT EXISTS date_of_birth DATE;
ALTER TABLE students ADD COLUMN IF NOT EXISTS gender VARCHAR(10);
ALTER TABLE students ADD COLUMN IF NOT EXISTS student_type VARCHAR(20);
ALTER TABLE students ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE students ADD COLUMN IF NOT EXISTS user_id BIGINT REFERENCES users(id);
ALTER TABLE students ADD COLUMN IF NOT EXISTS class_name VARCHAR(100);

-- users: the original CHECK constraint only allowed ADMIN/TEACHER — drop it so
-- STUDENT and PARENT roles can be inserted too
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
