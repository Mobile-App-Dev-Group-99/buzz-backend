package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.StudentParent;
import com.buzzapp.attendance_service.model.StudentParentId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {
}