package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.StudentParent;
import com.buzzapp.safety_service.model.StudentParentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {
    List<StudentParent> findByIdStudentId(Long studentId);
}
