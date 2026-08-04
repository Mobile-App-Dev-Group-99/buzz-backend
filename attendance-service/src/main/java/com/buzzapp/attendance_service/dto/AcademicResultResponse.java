package com.buzzapp.attendance_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
public class AcademicResultResponse {
    private Long studentId;
    private String studentName;
    private String className;
    private List<TermResultGroup> terms;

    @Data
    public static class TermResultGroup {
        private String term;
        private int year;
        private List<SubjectResult> subjects;
    }

    @Data
    public static class SubjectResult {
        private String subject;
        private BigDecimal score;
        private String grade;
        private String teacherRemark;
    }
}
