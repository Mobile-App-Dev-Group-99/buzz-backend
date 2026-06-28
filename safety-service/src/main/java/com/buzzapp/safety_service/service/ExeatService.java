package com.buzzapp.safety_service.service;

import com.buzzapp.safety_service.dto.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExeatService {

    public ExeatResponse createExeat(CreateExeatRequest request, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ExeatResponse approveExeat(Long exeatId, ApproveExeatRequest request, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ExeatResponse recordReturn(Long exeatId, ReturnExeatRequest request, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public List<ExeatResponse> getExeatsByStudent(Long studentId, Long schoolId) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}