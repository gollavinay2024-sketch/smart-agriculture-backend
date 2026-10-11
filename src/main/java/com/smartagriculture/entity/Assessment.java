package com.smartagriculture.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "assessments", indexes = {
        @Index(name = "idx_assessments_user", columnList = "userId"),
        @Index(name = "idx_assessments_crop", columnList = "cropType"),
        @Index(name = "idx_assessments_risk", columnList = "riskLevel"),
        @Index(name = "idx_assessments_created", columnList = "createdAt")
})
public class Assessment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @NotBlank(message = "Farmer name is required")
    @Column(nullable = false, length = 100)
    private String farmerName;

    @Column(length = 20)
    private String phoneNumber;

    @NotBlank(message = "Crop type is required")
    @Column(nullable = false, length = 50)
    private String cropType;

    @NotNull(message = "Harvest date is required")
    @Column(nullable = false)
    private LocalDate harvestDate;

    @NotNull(message = "Harvest time is required")
    @Column(nullable = false)
    private LocalTime harvestTime;

    @NotBlank(message = "Location name is required")
    @Column(nullable = false, length = 150)
    private String locationName;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    @Column(nullable = false, length = 20)
    private String riskLevel; // LOW, MEDIUM, HIGH

    private Integer riskScore; // 0 to 100 estimated score

    private Double precipitationProbability; // %
    private Double rainfallAmount; // mm
    private Double temperature; // °C
    private Double relativeHumidity; // %
    private Double windSpeed; // km/h

    @Column(length = 100)
    private String weatherCondition;

    @Column(columnDefinition = "TEXT")
    private String riskExplanation;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String voiceAlertText;

    private LocalDateTime forecastTimestamp;

    @Column(length = 50)
    private String dataStatus = "AVAILABLE"; // AVAILABLE, PARTIAL, OUT_OF_HORIZON

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Assessment() {}

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

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

    public String getRiskExplanation() { return riskExplanation; }
    public void setRiskExplanation(String riskExplanation) { this.riskExplanation = riskExplanation; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public String getVoiceAlertText() { return voiceAlertText; }
    public void setVoiceAlertText(String voiceAlertText) { this.voiceAlertText = voiceAlertText; }

    public LocalDateTime getForecastTimestamp() { return forecastTimestamp; }
    public void setForecastTimestamp(LocalDateTime forecastTimestamp) { this.forecastTimestamp = forecastTimestamp; }

    public String getDataStatus() { return dataStatus; }
    public void setDataStatus(String dataStatus) { this.dataStatus = dataStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
