package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceService {

    public ScanResponse recordScan(ScanRequest request, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public TodaySummaryResponse getTodaySummary(Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<LiveFeedEntry> getLiveFeed(Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<ClassAttendanceResponse> getClassesToday(Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public WeeklyAttendanceResponse getWeeklyRates(Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<StudentAttendanceEntry> getClassToday(Long classId, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<StudentAttendanceEntry> getStudentHistory(Long studentId, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public StudentTermSummaryResponse getStudentTermSummary(Long studentId, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public WeeklyCalendarResponse getStudentWeek(Long studentId, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}