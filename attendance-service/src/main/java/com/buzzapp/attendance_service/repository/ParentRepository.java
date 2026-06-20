package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, Long> {
}