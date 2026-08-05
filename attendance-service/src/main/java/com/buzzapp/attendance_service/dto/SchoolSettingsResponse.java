package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class SchoolSettingsResponse {
    private Long schoolId;
    private String arrivalCutoff;
    private boolean alertsAbsent;
    private boolean alertsLate;
    private boolean alertsExeat;
}
