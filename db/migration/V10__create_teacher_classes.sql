CREATE TABLE teacher_classes (
    id BIGSERIAL PRIMARY KEY,
    teacher_user_id BIGINT NOT NULL REFERENCES users(id),
    class_name VARCHAR(20) NOT NULL,
    school_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(teacher_user_id, school_id),
    UNIQUE(class_name, school_id)
);
