package com.buzzapp.safety_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReturnExeatRequest {
    private LocalDateTime actualReturn;
}