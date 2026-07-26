package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.StudentParent;
import com.buzzapp.attendance_service.model.StudentParentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {
    List<StudentParent> findByIdParentId(Long parentId);
    List<StudentParent> findByIdStudentId(Long studentId);
    @Transactional
    void deleteByStudentId(Long studentId);
    @Transactional
    void deleteByParentId(Long parentId);
}