package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class LinkRequest {
    private Long studentId;
    private Long parentId;
}
