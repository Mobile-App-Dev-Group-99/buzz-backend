package com.buzzapp.safety_service.repository;

import com.buzzapp.safety_service.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {
    Optional<Parent> findByUserId(Long userId);
}
