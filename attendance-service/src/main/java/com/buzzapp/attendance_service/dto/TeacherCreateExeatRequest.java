package com.buzzapp.attendance_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TeacherCreateExeatRequest {
    @NotNull
    private Long studentId;

    @NotBlank
    private String reason;

    private String expectedReturn;
}
