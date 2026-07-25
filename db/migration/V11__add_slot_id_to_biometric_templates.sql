-- V11__add_slot_id_to_biometric_templates.sql
-- Adds slot_id column to map scanner memory slots to students.
-- This allows the USB fingerprint scanner to identify a student by its internal slot ID.

ALTER TABLE biometric_templates ADD COLUMN slot_id INTEGER;
CREATE UNIQUE INDEX idx_biometric_slot_id ON biometric_templates (slot_id) WHERE slot_id IS NOT NULL;
