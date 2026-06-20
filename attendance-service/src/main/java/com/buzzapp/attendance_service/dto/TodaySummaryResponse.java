package com.buzzapp.attendance_service.dto;

import lombok.Data;

@Data
public class TodaySummaryResponse {
    private int presentToday;
    private int lateArrivals;
    private int absent;
    private int onExeat;
}