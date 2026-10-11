package com.smartagriculture.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class RiskAnalysisRequest {

    private String farmerName = "Farmer";
    private String phoneNumber;

    @NotBlank(message = "Crop type is required")
    private String cropType;

    @NotNull(message = "Harvest date is required")
    private LocalDate harvestDate;

    @NotNull(message = "Harvest time is required")
    private LocalTime harvestTime;

    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;

    private String locationName = "Current Location";

    private String language = "en"; // en, te, hi

    private boolean saveAssessment = true;

    public RiskAnalysisRequest() {}

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

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public boolean isSaveAssessment() { return saveAssessment; }
    public void setSaveAssessment(boolean saveAssessment) { this.saveAssessment = saveAssessment; }
}
