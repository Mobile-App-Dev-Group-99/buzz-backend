-- V12__add_performance_indexes.sql
-- Adds composite indexes for frequently queried columns.

-- Attendance events (most queried table)
CREATE INDEX IF NOT EXISTS idx_attendance_student_school_time
    ON attendance_events (student_id, school_id, scanned_at);
CREATE INDEX IF NOT EXISTS idx_attendance_school_time
    ON attendance_events (school_id, scanned_at);

-- Users
CREATE INDEX IF NOT EXISTS idx_users_school ON users (school_id);
CREATE INDEX IF NOT EXISTS idx_users_role_school ON users (role, school_id);

-- Students
CREATE INDEX IF NOT EXISTS idx_students_class_school ON students (school_id, class_name);

-- Exeats
CREATE INDEX IF NOT EXISTS idx_exeats_student_school ON exeats (student_id, school_id);

-- Notifications
CREATE INDEX IF NOT EXISTS idx_notifications_parent_school ON notifications (parent_id, school_id);
