package com.buzzapp.auth_service.repository;

import com.buzzapp.auth_service.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Long> {
}