-- School settings (per-school alert thresholds + arrival cutoff)
CREATE TABLE IF NOT EXISTS school_settings (
    school_id BIGINT PRIMARY KEY,
    arrival_cutoff VARCHAR(10),
    alerts_absent BOOLEAN NOT NULL DEFAULT TRUE,
    alerts_late BOOLEAN NOT NULL DEFAULT TRUE,
    alerts_exeat BOOLEAN NOT NULL DEFAULT TRUE
);

-- Per-user notification preferences (channels per alert category)
CREATE TABLE IF NOT EXISTS notification_preferences (
    id BIGSERIAL PRIMARY KEY,
    role VARCHAR(20) NOT NULL,
    recipient_id BIGINT NOT NULL,
    category VARCHAR(20) NOT NULL,
    push_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    email_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_notification_preferences UNIQUE (role, recipient_id, category)
);
