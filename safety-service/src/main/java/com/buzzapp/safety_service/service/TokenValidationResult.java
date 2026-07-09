package com.buzzapp.safety_service.service;

public record TokenValidationResult(boolean valid, String role, Long schoolId) {
}