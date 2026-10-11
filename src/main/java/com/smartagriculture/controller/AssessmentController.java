package com.smartagriculture.controller;

import com.smartagriculture.dto.RiskAnalysisRequest;
import com.smartagriculture.dto.RiskAnalysisResponse;
import com.smartagriculture.entity.Assessment;
import com.smartagriculture.service.AssessmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @PostMapping
    public ResponseEntity<RiskAnalysisResponse> createAssessment(@Valid @RequestBody RiskAnalysisRequest request) {
        request.setSaveAssessment(true);
        RiskAnalysisResponse response = assessmentService.analyzeAndSave(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<Assessment>> getAllAssessments(
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) String risk) {
        return ResponseEntity.ok(assessmentService.getAssessments(crop, risk));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Assessment> getAssessmentById(@PathVariable Long id) {
        return assessmentService.getAssessmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
