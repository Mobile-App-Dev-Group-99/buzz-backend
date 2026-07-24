package com.buzzapp.attendance_service.repository;

import com.buzzapp.attendance_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findBySchoolId(Long schoolId);
    List<User> findByRoleAndSchoolId(String role, Long schoolId);
}
