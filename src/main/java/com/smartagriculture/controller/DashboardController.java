package com.smartagriculture.controller;

import com.smartagriculture.dto.DashboardSummaryResponse;
import com.smartagriculture.service.AssessmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final AssessmentService assessmentService;

    public DashboardController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(assessmentService.getDashboardSummary());
    }
}
