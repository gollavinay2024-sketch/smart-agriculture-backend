package com.smartagriculture.controller;

import com.smartagriculture.dto.RiskAnalysisRequest;
import com.smartagriculture.dto.RiskAnalysisResponse;
import com.smartagriculture.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk")
public class RiskController {

    private final AssessmentService assessmentService;

    public RiskController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<RiskAnalysisResponse> analyzeRisk(@Valid @RequestBody RiskAnalysisRequest request) {
        RiskAnalysisResponse response = assessmentService.analyzeAndSave(request);
        return ResponseEntity.ok(response);
    }
}
