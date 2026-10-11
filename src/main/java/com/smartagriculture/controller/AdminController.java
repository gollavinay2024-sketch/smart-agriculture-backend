package com.smartagriculture.controller;

import com.smartagriculture.dto.AdminDashboardResponse;
import com.smartagriculture.dto.UserDto;
import com.smartagriculture.entity.Assessment;
import com.smartagriculture.repository.AssessmentRepository;
import com.smartagriculture.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserRepository userRepository;
    private final AssessmentRepository assessmentRepository;

    public AdminController(UserRepository userRepository, AssessmentRepository assessmentRepository) {
        this.userRepository = userRepository;
        this.assessmentRepository = assessmentRepository;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getAdminDashboard() {
        AdminDashboardResponse res = new AdminDashboardResponse();
        res.setTotalUsers(userRepository.count());
        res.setTotalAssessments(assessmentRepository.count());
        res.setLowRiskCount(assessmentRepository.countByRiskLevelIgnoreCase("LOW"));
        res.setMediumRiskCount(assessmentRepository.countByRiskLevelIgnoreCase("MEDIUM"));
        res.setHighRiskCount(assessmentRepository.countByRiskLevelIgnoreCase("HIGH"));

        // Recent users (safe UserDto)
        List<UserDto> users = userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .limit(10)
                .map(UserDto::new)
                .toList();
        res.setRecentUsers(users);

        // Recent assessments
        res.setRecentAssessments(assessmentRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 10)));

        return ResponseEntity.ok(res);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(UserDto::new)
                .toList();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/assessments")
    public ResponseEntity<List<Assessment>> getAllAssessments() {
        return ResponseEntity.ok(assessmentRepository.findAllByOrderByCreatedAtDesc());
    }
}
