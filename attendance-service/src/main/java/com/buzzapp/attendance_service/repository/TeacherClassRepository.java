package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.TeacherClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeacherClassRepository extends JpaRepository<TeacherClass, Long> {
    Optional<TeacherClass> findByTeacherUserIdAndSchoolId(Long teacherUserId, Long schoolId);
    Optional<TeacherClass> findByClassNameAndSchoolId(String className, Long schoolId);
    List<TeacherClass> findBySchoolId(Long schoolId);
    void deleteByTeacherUserIdAndSchoolId(Long teacherUserId, Long schoolId);
}
