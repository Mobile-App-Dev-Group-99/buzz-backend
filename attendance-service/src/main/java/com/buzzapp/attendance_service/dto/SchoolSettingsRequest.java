package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class SchoolSettingsRequest {
    private String arrivalCutoff;
    private Boolean alertsAbsent;
    private Boolean alertsLate;
    private Boolean alertsExeat;
}
