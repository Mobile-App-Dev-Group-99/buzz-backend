package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.StudentParent;
import com.buzzapp.attendance_service.model.StudentParentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentParentRepository extends JpaRepository<StudentParent, StudentParentId> {
    @Query("SELECT sp FROM StudentParent sp WHERE sp.id.parentId = :parentId")
    List<StudentParent> findByParentId(@Param("parentId") Long parentId);

    @Query("SELECT sp FROM StudentParent sp WHERE sp.id.studentId = :studentId")
    List<StudentParent> findByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("DELETE FROM StudentParent sp WHERE sp.id.studentId = :studentId")
    @Transactional
    void deleteByStudentId(@Param("studentId") Long studentId);

    @Modifying
    @Query("DELETE FROM StudentParent sp WHERE sp.id.parentId = :parentId")
    @Transactional
    void deleteByParentId(@Param("parentId") Long parentId);
}