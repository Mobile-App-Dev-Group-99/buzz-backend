package com.buzzapp.attendance_service.controller;

import com.buzzapp.attendance_service.dto.AcademicResultResponse;
import com.buzzapp.attendance_service.model.AcademicResult;
import com.buzzapp.attendance_service.model.Student;
import com.buzzapp.attendance_service.repository.AcademicResultRepository;
import com.buzzapp.attendance_service.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicController {

    private final AcademicResultRepository academicResultRepository;
    private final StudentRepository studentRepository;

    @GetMapping("/student/{studentId}")
    public ResponseEntity<?> getStudentResults(
            @PathVariable Long studentId,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null || !student.getSchoolId().equals(schoolId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        List<AcademicResult> results = academicResultRepository
                .findByStudentIdOrderByYearDescTermAscSubjectAsc(studentId);

        if (results.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        AcademicResultResponse response = buildResponse(student, results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}/term/{term}")
    public ResponseEntity<?> getStudentResultsByTerm(
            @PathVariable Long studentId,
            @PathVariable String term,
            Authentication auth) {
        Long schoolId = (Long) auth.getPrincipal();

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null || !student.getSchoolId().equals(schoolId)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Student not found"));
        }

        List<AcademicResult> results = academicResultRepository
                .findByStudentIdAndTermOrderBySubjectAsc(studentId, term);

        if (results.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        AcademicResultResponse response = buildResponse(student, results);
        return ResponseEntity.ok(response);
    }

    private AcademicResultResponse buildResponse(Student student, List<AcademicResult> results) {
        AcademicResultResponse response = new AcademicResultResponse();
        response.setStudentId(student.getId());
        response.setStudentName(student.getFirstName() + " " + student.getLastName());
        response.setClassName(student.getClassName());

        Map<String, List<AcademicResult>> byTerm = results.stream()
                .collect(Collectors.groupingBy(
                        r -> r.getTerm() + "|" + r.getYear(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<AcademicResultResponse.TermResultGroup> terms = new ArrayList<>();
        for (Map.Entry<String, List<AcademicResult>> entry : byTerm.entrySet()) {
            String[] parts = entry.getKey().split("\\|");
            String termName = parts[0];
            int year = Integer.parseInt(parts[1]);

            AcademicResultResponse.TermResultGroup termGroup =
                    new AcademicResultResponse.TermResultGroup();
            termGroup.setTerm(termName);
            termGroup.setYear(year);

            List<AcademicResultResponse.SubjectResult> subjects = new ArrayList<>();
            for (AcademicResult r : entry.getValue()) {
                AcademicResultResponse.SubjectResult sr =
                        new AcademicResultResponse.SubjectResult();
                sr.setSubject(r.getSubject());
                sr.setScore(r.getScore());
                sr.setGrade(r.getGrade());
                sr.setTeacherRemark(r.getTeacherRemark());
                subjects.add(sr);
            }
            termGroup.setSubjects(subjects);
            terms.add(termGroup);
        }
        response.setTerms(terms);
        return response;
    }
}
