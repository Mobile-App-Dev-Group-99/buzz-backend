package com.buzzapp.auth_service.repository;

import com.buzzapp.auth_service.model.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParentRepository extends JpaRepository<Parent, Long> {
}
