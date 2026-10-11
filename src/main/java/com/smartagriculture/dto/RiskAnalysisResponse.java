package com.smartagriculture.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RiskAnalysisResponse {

    private Long assessmentId;
    private String farmerName;
    private String cropType;
    private LocalDate harvestDate;
    private LocalTime harvestTime;
    private String locationName;
    private Double latitude;
    private Double longitude;

    private String riskLevel; // LOW, MEDIUM, HIGH
    private Integer riskScore; // 0 - 100 estimated score

    private Double precipitationProbability; // %
    private Double rainfallAmount; // mm
    private Double temperature; // °C
    private Double relativeHumidity; // %
    private Double windSpeed; // km/h
    private String weatherCondition;

    private List<String> contributingFactors = new ArrayList<>();
    private String riskExplanation;
    private List<String> recommendations = new ArrayList<>();
    private String voiceAlertText;

    private LocalDateTime forecastTimestamp;
    private boolean forecastHorizonExceeded = false;
    private String dataStatus = "AVAILABLE"; // AVAILABLE, PARTIAL, OUT_OF_HORIZON, ERROR
    private LocalDateTime assessmentTimestamp = LocalDateTime.now();

    public RiskAnalysisResponse() {}

    // Getters and setters
    public Long getAssessmentId() { return assessmentId; }
    public void setAssessmentId(Long assessmentId) { this.assessmentId = assessmentId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getCropType() { return cropType; }
    public void setCropType(String cropType) { this.cropType = cropType; }

    public LocalDate getHarvestDate() { return harvestDate; }
    public void setHarvestDate(LocalDate harvestDate) { this.harvestDate = harvestDate; }

    public LocalTime getHarvestTime() { return harvestTime; }
    public void setHarvestTime(LocalTime harvestTime) { this.harvestTime = harvestTime; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }

    public Integer getRiskScore() { return riskScore; }
    public void setRiskScore(Integer riskScore) { this.riskScore = riskScore; }

    public Double getPrecipitationProbability() { return precipitationProbability; }
    public void setPrecipitationProbability(Double precipitationProbability) { this.precipitationProbability = precipitationProbability; }

    public Double getRainfallAmount() { return rainfallAmount; }
    public void setRainfallAmount(Double rainfallAmount) { this.rainfallAmount = rainfallAmount; }

    public Double getTemperature() { return temperature; }
    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getRelativeHumidity() { return relativeHumidity; }
    public void setRelativeHumidity(Double relativeHumidity) { this.relativeHumidity = relativeHumidity; }

    public Double getWindSpeed() { return windSpeed; }
    public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

    public String getWeatherCondition() { return weatherCondition; }
    public void setWeatherCondition(String weatherCondition) { this.weatherCondition = weatherCondition; }

    public List<String> getContributingFactors() { return contributingFactors; }
    public void setContributingFactors(List<String> contributingFactors) { this.contributingFactors = contributingFactors; }

    public String getRiskExplanation() { return riskExplanation; }
    public void setRiskExplanation(String riskExplanation) { this.riskExplanation = riskExplanation; }

    public List<String> getRecommendations() { return recommendations; }
    public void setRecommendations(List<String> recommendations) { this.recommendations = recommendations; }

    public String getVoiceAlertText() { return voiceAlertText; }
    public void setVoiceAlertText(String voiceAlertText) { this.voiceAlertText = voiceAlertText; }

    public LocalDateTime getForecastTimestamp() { return forecastTimestamp; }
    public void setForecastTimestamp(LocalDateTime forecastTimestamp) { this.forecastTimestamp = forecastTimestamp; }

    public boolean isForecastHorizonExceeded() { return forecastHorizonExceeded; }
    public void setForecastHorizonExceeded(boolean forecastHorizonExceeded) { this.forecastHorizonExceeded = forecastHorizonExceeded; }

    public String getDataStatus() { return dataStatus; }
    public void setDataStatus(String dataStatus) { this.dataStatus = dataStatus; }

    public LocalDateTime getAssessmentTimestamp() { return assessmentTimestamp; }
    public void setAssessmentTimestamp(LocalDateTime assessmentTimestamp) { this.assessmentTimestamp = assessmentTimestamp; }
}
