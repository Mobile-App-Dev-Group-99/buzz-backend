package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.model.AttendanceEvent;
import com.buzzapp.attendance_service.model.AttendanceStatus;
import com.buzzapp.attendance_service.model.ScanType;
import com.buzzapp.attendance_service.model.Student;
import com.buzzapp.attendance_service.repository.AttendanceEventRepository;
import com.buzzapp.attendance_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportsService {

    private final AttendanceEventRepository attendanceEventRepository;
    private final StudentRepository studentRepository;

    public String getAttendanceReportCsv(Long schoolId) {
        long totalStudents = studentRepository.countBySchoolId(schoolId);

        StringBuilder csv = new StringBuilder();
        csv.append("Date,Present,Late,Absent,Total Students,Attendance Rate (%)\n");

        LocalDate today = LocalDate.now();
        for (int i = 29; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            List<AttendanceEvent> events = attendanceEventRepository
                    .findBySchoolIdAndScannedAtBetween(schoolId, day.atStartOfDay(), day.atTime(LocalTime.MAX));

            Map<Long, AttendanceEvent> latestArrival = new LinkedHashMap<>();
            events.stream()
                    .filter(e -> e.getScanType() == ScanType.ARRIVAL)
                    .sorted(Comparator.comparing(AttendanceEvent::getScannedAt))
                    .forEach(e -> latestArrival.put(e.getStudentId(), e));

            int present = latestArrival.size();
            long late = latestArrival.values().stream()
                    .filter(e -> e.getStatus() == AttendanceStatus.LATE)
                    .count();
            int absent = (int) Math.max(totalStudents - present, 0);
            double rate = totalStudents > 0 ? (present * 100.0 / totalStudents) : 0.0;

            csv.append(day.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    .append(',').append(present)
                    .append(',').append(late)
                    .append(',').append(absent)
                    .append(',').append(totalStudents)
                    .append(',').append(String.format(Locale.US, "%.1f", rate))
                    .append('\n');
        }
        return csv.toString();
    }

    public String getStudentsReportCsv(Long schoolId) {
        LocalDate termStart = LocalDate.now().withDayOfYear(1);
        LocalDate today = LocalDate.now();
        long totalSchoolDays = termStart.datesUntil(today.plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY && d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();

        List<Student> students = studentRepository.findBySchoolId(schoolId);
        List<AttendanceEvent> events = attendanceEventRepository
                .findBySchoolIdAndScannedAtBetween(schoolId, termStart.atStartOfDay(), today.atTime(LocalTime.MAX));

        Map<Long, Set<LocalDate>> presentDaysByStudent = new HashMap<>();
        for (AttendanceEvent e : events) {
            if (e.getScanType() != ScanType.ARRIVAL) continue;
            presentDaysByStudent
                    .computeIfAbsent(e.getStudentId(), k -> new HashSet<>())
                    .add(e.getScannedAt().toLocalDate());
        }

        StringBuilder csv = new StringBuilder();
        csv.append("Student ID,First Name,Last Name,Class,Days Present,School Days,Attendance Rate (%)\n");
        for (Student s : students) {
            int daysPresent = presentDaysByStudent.getOrDefault(s.getId(), Set.of()).size();
            double rate = totalSchoolDays > 0 ? (daysPresent * 100.0 / totalSchoolDays) : 0.0;
            csv.append(s.getId())
                    .append(',').append(escapeCsv(s.getFirstName()))
                    .append(',').append(escapeCsv(s.getLastName()))
                    .append(',').append(escapeCsv(s.getClassName()))
                    .append(',').append(daysPresent)
                    .append(',').append(totalSchoolDays)
                    .append(',').append(String.format(Locale.US, "%.1f", rate))
                    .append('\n');
        }
        return csv.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
