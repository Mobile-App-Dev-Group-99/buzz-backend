package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.StudentParent;
import com.buzzapp.safety_service.model.StudentParentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {
    @Query("SELECT sp FROM StudentParent sp WHERE sp.id.studentId = :studentId")
    List<StudentParent> findByStudentId(@Param("studentId") Long studentId);
}
