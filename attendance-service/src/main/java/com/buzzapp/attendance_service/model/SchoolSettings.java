package com.buzzapp.attendance_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "school_settings")
@Data
public class SchoolSettings {

    @Id
    private Long schoolId;

    @Column(length = 10)
    private String arrivalCutoff;

    @Column(nullable = false)
    private boolean alertsAbsent = true;

    @Column(nullable = false)
    private boolean alertsLate = true;

    @Column(nullable = false)
    private boolean alertsExeat = true;
}
