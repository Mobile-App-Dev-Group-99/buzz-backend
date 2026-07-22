package com.buzzapp.attendance_service.service;

import com.buzzapp.attendance_service.dto.*;
import com.buzzapp.attendance_service.model.*;
import com.buzzapp.attendance_service.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceEventRepository attendanceEventRepository;
    private final StudentRepository studentRepository;
    private final TeacherClassRepository teacherClassRepository;
    private final UserRepository userRepository;

    @Value("${attendance.arrival.cutoff}")
    private String arrivalCutoff;

    // ─── 1. POST /api/attendance/scan ────────────────────────────────────────

    public ScanResponse recordScan(ScanRequest request, Long schoolId) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found: " + request.getStudentId()));

        if (!student.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Student does not belong to this school");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalTime cutoff = LocalTime.parse(arrivalCutoff);

        boolean isLate = false;
        AttendanceStatus status;

        if (request.getScanType() == ScanType.ARRIVAL) {
            isLate = now.toLocalTime().isAfter(cutoff);
            status = isLate ? AttendanceStatus.LATE : AttendanceStatus.ARRIVED;
        } else {
            status = AttendanceStatus.DEPARTED;
        }

        AttendanceEvent event = new AttendanceEvent();
        event.setStudentId(student.getId());
        event.setSchoolId(schoolId);
        event.setScanType(request.getScanType());
        event.setScannedAt(now);
        event.setLate(isLate);
        event.setGate(request.getGate());
        event.setStatus(status);

        AttendanceEvent saved = attendanceEventRepository.save(event);

        ScanResponse response = new ScanResponse();
        response.setId(saved.getId());
        response.setStudentId(student.getId());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setScanType(saved.getScanType());
        response.setStatus(saved.getStatus());
        response.setLate(saved.isLate());
        response.setScannedAt(saved.getScannedAt());
        response.setGate(saved.getGate());

        return response;
    }

    // ─── 2. GET /api/attendance/summary/today ────────────────────────────────

    public TodaySummaryResponse getTodaySummary(Long schoolId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<AttendanceEvent> todayEvents = attendanceEventRepository
                .findBySchoolIdAndScannedAtBetween(schoolId, startOfDay, endOfDay);

        // One entry per student — take their most recent event of the day
        Map<Long, AttendanceEvent> latestPerStudent = new LinkedHashMap<>();
        todayEvents.stream()
                .sorted(Comparator.comparing(AttendanceEvent::getScannedAt))
                .forEach(e -> latestPerStudent.put(e.getStudentId(), e));

        int present = 0;
        int late = 0;

        for (AttendanceEvent e : latestPerStudent.values()) {
            if (e.getStatus() == AttendanceStatus.ARRIVED) present++;
            if (e.getStatus() == AttendanceStatus.LATE) { present++; late++; }
        }

        long totalStudents = studentRepository.countBySchoolId(schoolId);
        int absent = (int) totalStudents - latestPerStudent.size();

        TodaySummaryResponse response = new TodaySummaryResponse();
        response.setPresentToday(present);
        response.setLateArrivals(late);
        response.setAbsent(Math.max(absent, 0));
        response.setOnExeat(0); // TODO: wire to safety-service when available
        return response;
    }

    // ─── 3. GET /api/attendance/live ─────────────────────────────────────────

    public List<LiveFeedEntry> getLiveFeed(Long schoolId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<AttendanceEvent> events = attendanceEventRepository
                .findBySchoolIdAndScannedAtBetweenOrderByScannedAtDesc(schoolId, startOfDay, endOfDay);

        return events.stream().map(e -> {
            Student student = studentRepository.findById(e.getStudentId()).orElse(null);
            LiveFeedEntry entry = new LiveFeedEntry();
            entry.setStudentName(student != null
                    ? student.getFirstName() + " " + student.getLastName() : "Unknown");
            entry.setClassName(student != null ? student.getClassName() : "Unknown");
            entry.setGate(e.getGate());
            entry.setScanType(e.getScanType());
            entry.setStatus(e.getStatus());
            entry.setScannedAt(e.getScannedAt());
            return entry;
        }).collect(Collectors.toList());
    }

    // ─── 4. GET /api/attendance/classes/today ────────────────────────────────

    public List<ClassAttendanceResponse> getClassesToday(Long schoolId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<AttendanceEvent> todayEvents = attendanceEventRepository
                .findBySchoolIdAndScannedAtBetween(schoolId, startOfDay, endOfDay);

        // Latest event per student today
        Map<Long, AttendanceEvent> latestPerStudent = new LinkedHashMap<>();
        todayEvents.stream()
                .sorted(Comparator.comparing(AttendanceEvent::getScannedAt))
                .forEach(e -> latestPerStudent.put(e.getStudentId(), e));

        // All students in this school, grouped by class
        List<Student> allStudents = studentRepository.findBySchoolId(schoolId);
        Map<String, List<Student>> byClass = allStudents.stream()
                .collect(Collectors.groupingBy(Student::getClassName));

        List<ClassAttendanceResponse> result = new ArrayList<>();
        for (Map.Entry<String, List<Student>> entry : byClass.entrySet()) {
            String className = entry.getKey();
            List<Student> students = entry.getValue();
            int total = students.size();
            int present = (int) students.stream()
                    .filter(s -> {
                        AttendanceEvent e = latestPerStudent.get(s.getId());
                        return e != null &&
                                (e.getStatus() == AttendanceStatus.ARRIVED ||
                                        e.getStatus() == AttendanceStatus.LATE);
                    }).count();

            ClassAttendanceResponse r = new ClassAttendanceResponse();
            r.setClassName(className);
            r.setPresent(present);
            r.setTotal(total);
            r.setPercentage(total > 0 ? (present * 100.0 / total) : 0.0);
            result.add(r);
        }
        return result;
    }

    // ─── 5. GET /api/attendance/weekly ───────────────────────────────────────

    public WeeklyAttendanceResponse getWeeklyRates(Long schoolId) {
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(java.time.DayOfWeek.MONDAY);

        long totalStudents = studentRepository.countBySchoolId(schoolId);
        Map<String, Double> rateByDay = new LinkedHashMap<>();

        for (int i = 0; i < 5; i++) { // Mon–Fri
            LocalDate day = monday.plusDays(i);
            if (day.isAfter(today)) break; // don't project future days

            LocalDateTime start = day.atStartOfDay();
            LocalDateTime end = day.atTime(LocalTime.MAX);

            List<AttendanceEvent> events = attendanceEventRepository
                    .findBySchoolIdAndScannedAtBetween(schoolId, start, end);

            long distinctPresent = events.stream()
                    .filter(e -> e.getScanType() == ScanType.ARRIVAL)
                    .map(AttendanceEvent::getStudentId)
                    .distinct()
                    .count();

            double rate = totalStudents > 0 ? (distinctPresent * 100.0 / totalStudents) : 0.0;
            String dayName = day.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            rateByDay.put(dayName, rate);
        }

        WeeklyAttendanceResponse response = new WeeklyAttendanceResponse();
        response.setRateByDay(rateByDay);
        return response;
    }

    // ─── 6. GET /api/attendance/class/{className}/today ──────────────────────

    public List<StudentAttendanceEntry> getClassToday(String className, Long schoolId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        List<Student> students = studentRepository.findByClassNameAndSchoolId(className, schoolId);

        List<StudentAttendanceEntry> result = new ArrayList<>();
        for (Student student : students) {
            List<AttendanceEvent> events = attendanceEventRepository
                    .findByStudentIdAndSchoolIdAndScannedAtBetweenOrderByScannedAtDesc(
                            student.getId(), schoolId, startOfDay, endOfDay);

            StudentAttendanceEntry entry = new StudentAttendanceEntry();
            entry.setStudentId(student.getId());
            entry.setStudentName(student.getFirstName() + " " + student.getLastName());

            if (!events.isEmpty()) {
                AttendanceEvent latest = events.get(0);
                entry.setScanType(latest.getScanType());
                entry.setStatus(latest.getStatus());
                entry.setLate(latest.isLate());
                entry.setScannedAt(latest.getScannedAt());
                entry.setGate(latest.getGate());
            } else {
                entry.setStatus(AttendanceStatus.ABSENT);
                entry.setLate(false);
            }
            result.add(entry);
        }
        return result;
    }

    // ─── 7. GET /api/attendance/student/{studentId} ──────────────────────────

    public List<StudentAttendanceEntry> getStudentHistory(Long studentId, Long schoolId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!student.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Student does not belong to this school");
        }

        List<AttendanceEvent> events = attendanceEventRepository
                .findByStudentIdAndSchoolIdOrderByScannedAtDesc(studentId, schoolId);

        return events.stream().map(e -> {
            StudentAttendanceEntry entry = new StudentAttendanceEntry();
            entry.setScanType(e.getScanType());
            entry.setStatus(e.getStatus());
            entry.setLate(e.isLate());
            entry.setScannedAt(e.getScannedAt());
            entry.setGate(e.getGate());
            return entry;
        }).collect(Collectors.toList());
    }

    // ─── 8. GET /api/attendance/student/{studentId}/term ─────────────────────

    public StudentTermSummaryResponse getStudentTermSummary(Long studentId, Long schoolId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!student.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Student does not belong to this school");
        }

        // Term = current calendar year for simplicity; can be refined later
        LocalDateTime termStart = LocalDate.now().withDayOfYear(1).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<AttendanceEvent> termEvents = attendanceEventRepository
                .findByStudentIdAndSchoolIdAndScannedAtBetween(studentId, schoolId, termStart, now);

        // Distinct days with an ARRIVAL scan
        Set<LocalDate> presentDays = termEvents.stream()
                .filter(e -> e.getScanType() == ScanType.ARRIVAL)
                .map(e -> e.getScannedAt().toLocalDate())
                .collect(Collectors.toSet());

        // Total school days = weekdays from term start to today
        long totalSchoolDays = termStart.toLocalDate().datesUntil(LocalDate.now().plusDays(1))
                .filter(d -> d.getDayOfWeek() != DayOfWeek.SATURDAY &&
                        d.getDayOfWeek() != DayOfWeek.SUNDAY)
                .count();

        int daysPresent = presentDays.size();
        double percentage = totalSchoolDays > 0 ? (daysPresent * 100.0 / totalSchoolDays) : 0.0;

        // Current streak — consecutive present days going back from today
        int streak = 0;
        LocalDate check = LocalDate.now();
        while (true) {
            if (check.getDayOfWeek() == DayOfWeek.SATURDAY ||
                    check.getDayOfWeek() == DayOfWeek.SUNDAY) {
                check = check.minusDays(1);
                continue;
            }
            if (presentDays.contains(check)) {
                streak++;
                check = check.minusDays(1);
            } else {
                break;
            }
        }

        StudentTermSummaryResponse response = new StudentTermSummaryResponse();
        response.setAttendancePercentage(percentage);
        response.setCurrentStreak(streak);
        response.setTotalDaysPresent(daysPresent);
        response.setTotalSchoolDays((int) totalSchoolDays);
        return response;
    }

    // ─── 9. GET /api/attendance/student/{studentId}/week ─────────────────────

    public WeeklyCalendarResponse getStudentWeek(Long studentId, Long schoolId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        if (!student.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Student does not belong to this school");
        }

        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = monday.atStartOfDay();
        LocalDateTime weekEnd = today.atTime(LocalTime.MAX);

        List<AttendanceEvent> weekEvents = attendanceEventRepository
                .findByStudentIdAndSchoolIdAndScannedAtBetween(studentId, schoolId, weekStart, weekEnd);

        Map<LocalDate, AttendanceStatus> week = new LinkedHashMap<>();
        for (int i = 0; i < 5; i++) {
            LocalDate day = monday.plusDays(i);
            if (day.isAfter(today)) break;

            LocalDate finalDay = day;
            AttendanceStatus status = weekEvents.stream()
                    .filter(e -> e.getScannedAt().toLocalDate().equals(finalDay) &&
                            e.getScanType() == ScanType.ARRIVAL)
                    .max(Comparator.comparing(AttendanceEvent::getScannedAt))
                    .map(AttendanceEvent::getStatus)
                    .orElse(AttendanceStatus.ABSENT);

            week.put(day, status);
        }

        WeeklyCalendarResponse response = new WeeklyCalendarResponse();
        response.setWeek(week);
        return response;
    }

    // ─── 10. POST /api/attendance/manual ─────────────────────────────────────

    public ScanResponse markManualAttendance(ManualAttendanceRequest request, Long schoolId, String teacherEmail) {
        User teacher = userRepository.findByEmail(teacherEmail)
                .orElseThrow(() -> new RuntimeException("Teacher not found"));

        Optional<TeacherClass> tc = teacherClassRepository.findByTeacherUserIdAndSchoolId(teacher.getId(), schoolId);
        if (tc.isEmpty()) {
            throw new RuntimeException("You are not assigned to any class");
        }

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getSchoolId().equals(schoolId)) {
            throw new RuntimeException("Student does not belong to this school");
        }
        if (!tc.get().getClassName().equals(student.getClassName())) {
            throw new RuntimeException("Student is not in your class");
        }

        LocalDateTime now = LocalDateTime.now();
        AttendanceStatus status;
        boolean isLate = false;

        switch (request.getStatus().toUpperCase()) {
            case "PRESENT":
                status = AttendanceStatus.ARRIVED;
                break;
            case "LATE":
                status = AttendanceStatus.LATE;
                isLate = true;
                break;
            case "ABSENT":
                status = AttendanceStatus.ABSENT;
                break;
            default:
                throw new RuntimeException("Invalid status: " + request.getStatus());
        }

        AttendanceEvent event = new AttendanceEvent();
        event.setStudentId(student.getId());
        event.setSchoolId(schoolId);
        event.setScanType(ScanType.ARRIVAL);
        event.setScannedAt(now);
        event.setLate(isLate);
        event.setGate("Manual");
        event.setStatus(status);

        AttendanceEvent saved = attendanceEventRepository.save(event);

        ScanResponse response = new ScanResponse();
        response.setId(saved.getId());
        response.setStudentId(student.getId());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setScanType(saved.getScanType());
        response.setStatus(saved.getStatus());
        response.setLate(saved.isLate());
        response.setScannedAt(saved.getScannedAt());
        response.setGate(saved.getGate());

        return response;
    }

    // ─── 11. GET /api/attendance/class/{className}/roster ────────────────────

    public List<Map<String, Object>> getClassRoster(String className, Long schoolId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        List<Student> students = studentRepository.findByClassNameAndSchoolId(className, schoolId);

        List<StudentAttendanceEntry> todayEntries = getClassToday(className, schoolId);

        Map<Long, StudentAttendanceEntry> entryByStudent = new LinkedHashMap<>();
        for (Student s : students) {
            for (StudentAttendanceEntry e : todayEntries) {
                if (e.getStudentId() != null && e.getStudentId().equals(s.getId())) {
                    entryByStudent.put(s.getId(), e);
                    break;
                }
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Student s : students) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", s.getId());
            item.put("firstName", s.getFirstName());
            item.put("lastName", s.getLastName());
            item.put("className", s.getClassName());

            StudentAttendanceEntry todayEntry = entryByStudent.get(s.getId());
            if (todayEntry != null && todayEntry.getStatus() != null) {
                item.put("todayStatus", todayEntry.getStatus().name());
                item.put("todayLate", todayEntry.isLate());
            } else {
                item.put("todayStatus", "NOT_MARKED");
                item.put("todayLate", false);
            }
            result.add(item);
        }
        return result;
    }
}