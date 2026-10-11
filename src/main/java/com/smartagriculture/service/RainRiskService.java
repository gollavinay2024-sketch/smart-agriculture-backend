package com.smartagriculture.service;

import com.smartagriculture.dto.RiskAnalysisRequest;
import com.smartagriculture.dto.RiskAnalysisResponse;
import com.smartagriculture.dto.WeatherForecastDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RainRiskService {

    private final CropRecommendationService cropRecommendationService;

    @Value("${app.risk.high-probability-threshold:70.0}")
    private double highProbabilityThreshold;

    @Value("${app.risk.medium-probability-threshold:40.0}")
    private double mediumProbabilityThreshold;

    @Value("${app.risk.high-rainfall-threshold:5.0}")
    private double highRainfallThreshold;

    @Value("${app.risk.medium-rainfall-threshold:1.5}")
    private double mediumRainfallThreshold;

    @Value("${app.risk.high-wind-threshold:35.0}")
    private double highWindThreshold;

    public RainRiskService(CropRecommendationService cropRecommendationService) {
        this.cropRecommendationService = cropRecommendationService;
    }

    /**
     * Evaluates rain risk using transparent agronomic thresholds
     */
    public RiskAnalysisResponse evaluateRisk(RiskAnalysisRequest request, WeatherForecastDto forecast) {
        RiskAnalysisResponse response = new RiskAnalysisResponse();
        response.setFarmerName(request.getFarmerName());
        response.setCropType(request.getCropType());
        response.setHarvestDate(request.getHarvestDate());
        response.setHarvestTime(request.getHarvestTime());
        response.setLocationName(request.getLocationName());
        response.setLatitude(request.getLatitude());
        response.setLongitude(request.getLongitude());

        if ("OUT_OF_HORIZON".equals(forecast.getDataStatus())) {
            response.setRiskLevel("UNKNOWN");
            response.setRiskScore(null);
            response.setForecastHorizonExceeded(true);
            response.setDataStatus("OUT_OF_HORIZON");
            response.setRiskExplanation("The selected harvest date (" + request.getHarvestDate() + ") exceeds the 14-day weather forecasting horizon. Open-Meteo numerical atmospheric models cannot reliably simulate rain risk beyond 14 days. Please re-check when the date is within 14 days.");
            response.getContributingFactors().add("Harvest date is beyond 14-day forecast model horizon.");
            response.setRecommendations(List.of(
                    "Re-evaluate rain risk closer to the harvest date (within 7 to 10 days).",
                    "Maintain standard pre-harvest monitoring without relying on long-range speculation."
            ));
            response.setVoiceAlertText(generateVoiceAlertText("UNKNOWN", request, null, null, null));
            return response;
        }

        if ("INVALID_DATE".equals(forecast.getDataStatus())) {
            response.setRiskLevel("UNKNOWN");
            response.setDataStatus("INVALID_DATE");
            response.setRiskExplanation(forecast.getMessage());
            return response;
        }

        WeatherForecastDto.HarvestSlotWeather slot = forecast.getTargetSlot();
        if (slot == null) {
            response.setRiskLevel("UNKNOWN");
            response.setDataStatus("NO_DATA");
            response.setRiskExplanation("No forecast data was available for the specified harvest time.");
            response.setRecommendations(List.of("Verify internet connection and retry weather retrieval."));
            return response;
        }

        Double prob = slot.getPrecipitationProbability();
        Double rain = slot.getPrecipitationAmount();
        Double wind = slot.getWindSpeed();
        Double temp = slot.getTemperature();
        Double humidity = slot.getRelativeHumidity();

        response.setPrecipitationProbability(prob);
        response.setRainfallAmount(rain);
        response.setTemperature(temp);
        response.setRelativeHumidity(humidity);
        response.setWindSpeed(wind);
        response.setWeatherCondition(slot.getWeatherDescription());
        response.setForecastTimestamp(slot.getTime());

        List<String> factors = new ArrayList<>();
        String riskLevel;
        int estimatedScore;

        boolean highByProb = prob != null && prob >= highProbabilityThreshold;
        boolean highByRain = rain != null && rain >= highRainfallThreshold;
        boolean mediumByProb = prob != null && prob >= mediumProbabilityThreshold;
        boolean mediumByRain = rain != null && rain >= mediumRainfallThreshold;
        boolean highWind = wind != null && wind >= highWindThreshold;

        if (prob != null) {
            factors.add(String.format("Forecast precipitation probability: %.0f%% (Thresholds: Low <%.0f%%, Med >=%.0f%%, High >=%.0f%%)",
                    prob, mediumProbabilityThreshold, mediumProbabilityThreshold, highProbabilityThreshold));
        } else {
            factors.add("Precipitation probability not provided by model; relying on rainfall mm and weather code.");
            response.setDataStatus("PARTIAL");
        }

        if (rain != null) {
            factors.add(String.format("Expected precipitation amount: %.1f mm (Thresholds: Low <%.1f mm, Med >=%.1f mm, High >=%.1f mm)",
                    rain, mediumRainfallThreshold, mediumRainfallThreshold, highRainfallThreshold));
        }

        if (wind != null && wind > 25.0) {
            factors.add(String.format("Wind speed: %.1f km/h%s", wind, highWind ? " (High wind hazard for standing crops)" : ""));
        }

        if (highByProb || highByRain) {
            riskLevel = "HIGH";
            double probComponent = prob != null ? (prob / 100.0) * 60.0 : 45.0;
            double rainComponent = rain != null ? Math.min(30.0, (rain / 10.0) * 30.0) : 25.0;
            double windComponent = highWind ? 10.0 : 0.0;
            estimatedScore = (int) Math.min(99, Math.max(70, Math.round(probComponent + rainComponent + windComponent)));
        } else if (mediumByProb || mediumByRain) {
            riskLevel = "MEDIUM";
            double probComponent = prob != null ? (prob / 100.0) * 50.0 : 30.0;
            double rainComponent = rain != null ? Math.min(25.0, (rain / 5.0) * 20.0) : 15.0;
            estimatedScore = (int) Math.min(69, Math.max(40, Math.round(probComponent + rainComponent)));
        } else {
            riskLevel = "LOW";
            double probComponent = prob != null ? (prob / 100.0) * 25.0 : 10.0;
            double rainComponent = rain != null ? Math.min(10.0, rain * 5.0) : 5.0;
            estimatedScore = (int) Math.min(39, Math.max(5, Math.round(probComponent + rainComponent)));
        }

        response.setRiskLevel(riskLevel);
        response.setRiskScore(estimatedScore);
        response.setContributingFactors(factors);

        // Explanation text
        String explanation = switch (riskLevel) {
            case "HIGH" -> String.format(
                    "High Rain Risk: Significant rain exposure is forecast (%.0f%% probability, %.1f mm precipitation). Harvesting without immediate shelter creates severe risk of crop wetting, grain discolouration, or post-harvest mold.",
                    prob != null ? prob : 0.0, rain != null ? rain : 0.0);
            case "MEDIUM" -> String.format(
                    "Medium Rain Risk: Moderate atmospheric instability is forecast (%.0f%% probability, %.1f mm precipitation). Spot showers or humidity may compromise open threshing floors.",
                    prob != null ? prob : 0.0, rain != null ? rain : 0.0);
            default -> String.format(
                    "Low Rain Risk: Atmospheric models indicate low probability of rainfall (%.0f%% probability, %.1f mm precipitation). Conditions are currently favorable for planned harvest activities.",
                    prob != null ? prob : 0.0, rain != null ? rain : 0.0);
        };
        response.setRiskExplanation(explanation);

        // Recommendations
        List<String> recommendations = cropRecommendationService.getRecommendations(request.getCropType(), riskLevel, request.getLanguage());
        response.setRecommendations(recommendations);

        // Voice Alert
        String voiceText = generateVoiceAlertText(riskLevel, request, prob, rain, response.getRecommendations());
        response.setVoiceAlertText(voiceText);

        return response;
    }

    private String generateVoiceAlertText(String riskLevel, RiskAnalysisRequest req, Double prob, Double rain, List<String> recs) {
        String lang = req.getLanguage() != null ? req.getLanguage().toLowerCase() : "en";
        String farmer = (req.getFarmerName() != null && !req.getFarmerName().isBlank()) ? req.getFarmerName() : "Farmer";
        String crop = req.getCropType() != null ? req.getCropType() : "crop";
        String location = req.getLocationName() != null ? req.getLocationName() : "your farm";

        if ("te".equals(lang)) {
            String riskTe = switch (riskLevel) {
                case "HIGH" -> "అధిక ప్రమాదం";
                case "MEDIUM" -> "మధ్యస్థ ప్రమాదం";
                case "LOW" -> "తక్కువ ప్రమాదం";
                default -> "తెలియదు";
            };
            return String.format("రైతు %s గారూ నమస్కారం. %s లో మీ %s పంట కోతకు వర్షం ముప్పు %sగా ఉంది. దయచేసి సిఫార్సు చేసిన రక్షణ చర్యలు పాటించండి.",
                    farmer, location, crop, riskTe);
        } else if ("hi".equals(lang)) {
            String riskHi = switch (riskLevel) {
                case "HIGH" -> "उच्च जोखिम";
                case "MEDIUM" -> "मध्यम जोखिम";
                case "LOW" -> "कम जोखिम";
                default -> "अज्ञात";
            };
            return String.format("नमस्ते किसान %s जी। %s में आपकी %s फसल कटाई के लिए बारिश का %s है। कृपया जरूरी सुरक्षा उपाय अपनाएं।",
                    farmer, location, crop, riskHi);
        } else {
            String primaryRec = (recs != null && !recs.isEmpty()) ? recs.get(0) : "Stay vigilant of weather shifts.";
            return String.format("Attention %s. Rain risk for your %s harvest in %s is %s. %s",
                    farmer, crop, location, riskLevel, primaryRec);
        }
    }
}
