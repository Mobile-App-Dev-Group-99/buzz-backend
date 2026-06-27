package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ExeatService {

    public ExeatResponse create(ExeatCreateRequest request, Long schoolId) {
        throw new UnsupportedOperationException("create() not yet implemented");
    }

    public ExeatResponse approve(Long exeatId, Long approvedBy, Long schoolId) {
        throw new UnsupportedOperationException("approve() not yet implemented");
    }

    public ExeatResponse recordReturn(Long exeatId, ExeatReturnRequest request, Long schoolId) {
        throw new UnsupportedOperationException("recordReturn() not yet implemented");
    }

    public List<ExeatResponse> getByStudent(Long studentId, Long schoolId) {
        throw new UnsupportedOperationException("getByStudent() not yet implemented");
    }
}