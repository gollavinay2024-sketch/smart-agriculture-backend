package com.smartagriculture.service;

import com.smartagriculture.dto.DashboardSummaryResponse;
import com.smartagriculture.dto.RiskAnalysisRequest;
import com.smartagriculture.dto.RiskAnalysisResponse;
import com.smartagriculture.dto.WeatherForecastDto;
import com.smartagriculture.entity.Assessment;
import com.smartagriculture.entity.Farmer;
import com.smartagriculture.entity.User;
import com.smartagriculture.repository.AssessmentRepository;
import com.smartagriculture.repository.FarmerRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class AssessmentService {

    private final WeatherService weatherService;
    private final RainRiskService rainRiskService;
    private final AssessmentRepository assessmentRepository;
    private final FarmerRepository farmerRepository;
    private final AuthService authService;

    public AssessmentService(WeatherService weatherService,
                             RainRiskService rainRiskService,
                             AssessmentRepository assessmentRepository,
                             FarmerRepository farmerRepository,
                             AuthService authService) {
        this.weatherService = weatherService;
        this.rainRiskService = rainRiskService;
        this.assessmentRepository = assessmentRepository;
        this.farmerRepository = farmerRepository;
        this.authService = authService;
    }

    /**
     * Executes risk analysis and optionally persists the assessment
     */
    @Transactional
    public RiskAnalysisResponse analyzeAndSave(RiskAnalysisRequest request) {
        Optional<User> currentUserOpt = authService.getCurrentUser();

        // Populate defaults from authenticated user if available
        if (currentUserOpt.isPresent()) {
            User currentUser = currentUserOpt.get();
            if (request.getFarmerName() == null || request.getFarmerName().isBlank() || "Farmer".equals(request.getFarmerName())) {
                request.setFarmerName(currentUser.getFullName());
            }
            if (request.getPhoneNumber() == null || request.getPhoneNumber().isBlank()) {
                request.setPhoneNumber(currentUser.getPhoneNumber());
            }
        }

        WeatherForecastDto forecast = weatherService.getForecast(
                request.getLatitude(),
                request.getLongitude(),
                request.getHarvestDate(),
                request.getHarvestTime()
        );

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(request, forecast);

        // Save assessment if requested and risk status is valid
        if (request.isSaveAssessment() && !"UNKNOWN".equals(response.getRiskLevel())) {
            Assessment assessment = new Assessment();
            currentUserOpt.ifPresent(u -> assessment.setUserId(u.getId()));

            assessment.setFarmerName(request.getFarmerName());
            assessment.setPhoneNumber(request.getPhoneNumber());
            assessment.setCropType(request.getCropType());
            assessment.setHarvestDate(request.getHarvestDate());
            assessment.setHarvestTime(request.getHarvestTime());
            assessment.setLocationName(request.getLocationName());
            assessment.setLatitude(request.getLatitude());
            assessment.setLongitude(request.getLongitude());
            assessment.setRiskLevel(response.getRiskLevel());
            assessment.setRiskScore(response.getRiskScore());
            assessment.setPrecipitationProbability(response.getPrecipitationProbability());
            assessment.setRainfallAmount(response.getRainfallAmount());
            assessment.setTemperature(response.getTemperature());
            assessment.setRelativeHumidity(response.getRelativeHumidity());
            assessment.setWindSpeed(response.getWindSpeed());
            assessment.setWeatherCondition(response.getWeatherCondition());
            assessment.setRiskExplanation(response.getRiskExplanation());
            assessment.setRecommendations(String.join(" | ", response.getRecommendations()));
            assessment.setVoiceAlertText(response.getVoiceAlertText());
            assessment.setForecastTimestamp(response.getForecastTimestamp());
            assessment.setDataStatus(response.getDataStatus());

            Assessment saved = assessmentRepository.save(assessment);
            response.setAssessmentId(saved.getId());

            // Save or update Farmer record if phone or name is present
            if (request.getFarmerName() != null && !request.getFarmerName().isBlank()) {
                saveOrUpdateFarmer(request);
            }
        }

        return response;
    }

    private void saveOrUpdateFarmer(RiskAnalysisRequest request) {
        try {
            Optional<Farmer> existing = Optional.empty();
            if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
                existing = farmerRepository.findByPhoneNumber(request.getPhoneNumber());
            }
            if (existing.isEmpty()) {
                existing = farmerRepository.findByName(request.getFarmerName());
            }

            Farmer farmer = existing.orElseGet(Farmer::new);
            farmer.setName(request.getFarmerName());
            if (request.getPhoneNumber() != null) farmer.setPhoneNumber(request.getPhoneNumber());
            if (request.getLanguage() != null) farmer.setPreferredLanguage(request.getLanguage());
            if (request.getLocationName() != null) farmer.setVillageLocation(request.getLocationName());
            if (request.getLatitude() != null) farmer.setLatitude(request.getLatitude());
            if (request.getLongitude() != null) farmer.setLongitude(request.getLongitude());

            farmerRepository.save(farmer);
        } catch (Exception ignored) {}
    }

    @Transactional(readOnly = true)
    public List<Assessment> getAssessments(String cropType, String riskLevel) {
        boolean hasCrop = cropType != null && !cropType.isBlank() && !"all".equalsIgnoreCase(cropType);
        boolean hasRisk = riskLevel != null && !riskLevel.isBlank() && !"all".equalsIgnoreCase(riskLevel);

        Optional<User> currentUserOpt = authService.getCurrentUser();
        boolean isAdmin = currentUserOpt.map(u -> "ROLE_ADMIN".equals(u.getRole())).orElse(false);

        // If regular user is authenticated, scope records strictly to their own account
        if (currentUserOpt.isPresent() && !isAdmin) {
            Long userId = currentUserOpt.get().getId();
            if (hasCrop && hasRisk) {
                return assessmentRepository.findByUserIdAndCropTypeIgnoreCaseAndRiskLevelIgnoreCaseOrderByCreatedAtDesc(userId, cropType, riskLevel);
            } else if (hasCrop) {
                return assessmentRepository.findByUserIdAndCropTypeIgnoreCaseOrderByCreatedAtDesc(userId, cropType);
            } else if (hasRisk) {
                return assessmentRepository.findByUserIdAndRiskLevelIgnoreCaseOrderByCreatedAtDesc(userId, riskLevel);
            } else {
                return assessmentRepository.findByUserIdOrderByCreatedAtDesc(userId);
            }
        }

        // Admin or legacy global view
        if (hasCrop && hasRisk) {
            return assessmentRepository.findByCropTypeIgnoreCaseAndRiskLevelIgnoreCaseOrderByCreatedAtDesc(cropType, riskLevel);
        } else if (hasCrop) {
            return assessmentRepository.findByCropTypeIgnoreCaseOrderByCreatedAtDesc(cropType);
        } else if (hasRisk) {
            return assessmentRepository.findByRiskLevelIgnoreCaseOrderByCreatedAtDesc(riskLevel);
        } else {
            return assessmentRepository.findAllByOrderByCreatedAtDesc();
        }
    }

    @Transactional(readOnly = true)
    public Optional<Assessment> getAssessmentById(Long id) {
        Optional<Assessment> assessmentOpt = assessmentRepository.findById(id);
        if (assessmentOpt.isEmpty()) {
            return Optional.empty();
        }

        Assessment assessment = assessmentOpt.get();
        Optional<User> currentUserOpt = authService.getCurrentUser();

        // Enforce user privacy isolation
        if (currentUserOpt.isPresent()) {
            User user = currentUserOpt.get();
            boolean isAdmin = "ROLE_ADMIN".equals(user.getRole());
            if (!isAdmin && assessment.getUserId() != null && !assessment.getUserId().equals(user.getId())) {
                throw new AccessDeniedException("You are not authorized to view this private assessment record.");
            }
        }

        return Optional.of(assessment);
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getDashboardSummary() {
        DashboardSummaryResponse summary = new DashboardSummaryResponse();

        Optional<User> currentUserOpt = authService.getCurrentUser();
        boolean isAdmin = currentUserOpt.map(u -> "ROLE_ADMIN".equals(u.getRole())).orElse(false);

        if (currentUserOpt.isPresent() && !isAdmin) {
            Long userId = currentUserOpt.get().getId();
            long total = assessmentRepository.countByUserId(userId);
            summary.setTotalAssessments(total);
            summary.setLowRiskCount(assessmentRepository.countByUserIdAndRiskLevelIgnoreCase(userId, "LOW"));
            summary.setMediumRiskCount(assessmentRepository.countByUserIdAndRiskLevelIgnoreCase(userId, "MEDIUM"));
            summary.setHighRiskCount(assessmentRepository.countByUserIdAndRiskLevelIgnoreCase(userId, "HIGH"));

            List<Object[]> cropRows = assessmentRepository.countGroupByCropTypeForUser(userId);
            Map<String, Long> cropMap = new HashMap<>();
            for (Object[] row : cropRows) {
                if (row[0] != null && row[1] != null) {
                    cropMap.put(row[0].toString(), ((Number) row[1]).longValue());
                }
            }
            summary.setCropDistribution(cropMap);
            summary.setRecentAssessments(assessmentRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 5)));
            return summary;
        }

        // Admin view or global summary
        long total = assessmentRepository.count();
        summary.setTotalAssessments(total);
        summary.setLowRiskCount(assessmentRepository.countByRiskLevelIgnoreCase("LOW"));
        summary.setMediumRiskCount(assessmentRepository.countByRiskLevelIgnoreCase("MEDIUM"));
        summary.setHighRiskCount(assessmentRepository.countByRiskLevelIgnoreCase("HIGH"));

        List<Object[]> cropRows = assessmentRepository.countGroupByCropType();
        Map<String, Long> cropMap = new HashMap<>();
        for (Object[] row : cropRows) {
            if (row[0] != null && row[1] != null) {
                cropMap.put(row[0].toString(), ((Number) row[1]).longValue());
            }
        }
        summary.setCropDistribution(cropMap);
        summary.setRecentAssessments(assessmentRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)));

        return summary;
    }
}
