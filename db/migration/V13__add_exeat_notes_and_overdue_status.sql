-- V13__add_exeat_notes_and_overdue_status.sql

-- Add notes column to exeats for additional context from students/teachers
ALTER TABLE exeats ADD COLUMN IF NOT EXISTS notes TEXT;

-- Allow OVERDUE in exeats status (previously only allowed PENDING/APPROVED/DENIED/RETURNED)
ALTER TABLE exeats DROP CONSTRAINT IF EXISTS exeats_status_check;
ALTER TABLE exeats ADD CONSTRAINT exeats_status_check CHECK (status IN ('PENDING', 'APPROVED', 'DENIED', 'RETURNED', 'OVERDUE'));
