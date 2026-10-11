package com.smartagriculture;

import com.smartagriculture.dto.RiskAnalysisRequest;
import com.smartagriculture.dto.RiskAnalysisResponse;
import com.smartagriculture.dto.WeatherForecastDto;
import com.smartagriculture.service.CropRecommendationService;
import com.smartagriculture.service.RainRiskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class RainRiskServiceTest {

    private RainRiskService rainRiskService;
    private CropRecommendationService cropRecommendationService;

    @BeforeEach
    void setUp() {
        cropRecommendationService = new CropRecommendationService();
        rainRiskService = new RainRiskService(cropRecommendationService);
        ReflectionTestUtils.setField(rainRiskService, "highProbabilityThreshold", 70.0);
        ReflectionTestUtils.setField(rainRiskService, "mediumProbabilityThreshold", 40.0);
        ReflectionTestUtils.setField(rainRiskService, "highRainfallThreshold", 5.0);
        ReflectionTestUtils.setField(rainRiskService, "mediumRainfallThreshold", 1.5);
        ReflectionTestUtils.setField(rainRiskService, "highWindThreshold", 35.0);
    }

    private RiskAnalysisRequest createSampleRequest() {
        RiskAnalysisRequest req = new RiskAnalysisRequest();
        req.setFarmerName("Ramesh");
        req.setPhoneNumber("9876543210");
        req.setCropType("Rice");
        req.setHarvestDate(LocalDate.now().plusDays(2));
        req.setHarvestTime(LocalTime.of(10, 0));
        req.setLatitude(17.3850);
        req.setLongitude(78.4867);
        req.setLocationName("Hyderabad");
        req.setLanguage("en");
        return req;
    }

    private WeatherForecastDto createForecast(Double prob, Double rain, Double wind, Double temp, Double humidity) {
        WeatherForecastDto dto = new WeatherForecastDto();
        dto.setDataStatus("AVAILABLE");
        WeatherForecastDto.HarvestSlotWeather slot = new WeatherForecastDto.HarvestSlotWeather();
        slot.setTime(LocalDateTime.now().plusDays(2));
        slot.setPrecipitationProbability(prob);
        slot.setPrecipitationAmount(rain);
        slot.setWindSpeed(wind);
        slot.setTemperature(temp);
        slot.setRelativeHumidity(humidity);
        slot.setWeatherCode(prob != null && prob > 60 ? 61 : 1);
        slot.setWeatherDescription("Rain");
        dto.setTargetSlot(slot);
        return dto;
    }

    @Test
    void testHighRiskClassification_ByPrecipitationProbability() {
        RiskAnalysisRequest req = createSampleRequest();
        WeatherForecastDto forecast = createForecast(85.0, 6.5, 20.0, 26.0, 80.0);

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(req, forecast);

        assertEquals("HIGH", response.getRiskLevel());
        assertTrue(response.getRiskScore() >= 70);
        assertNotNull(response.getRiskExplanation());
        assertTrue(response.getRiskExplanation().contains("High Rain Risk"));
        assertFalse(response.getRecommendations().isEmpty());
        assertTrue(response.getRecommendations().get(0).contains("POSTPONE HARVEST"));
        assertNotNull(response.getVoiceAlertText());
    }

    @Test
    void testMediumRiskClassification_ByModeratePrecipitation() {
        RiskAnalysisRequest req = createSampleRequest();
        WeatherForecastDto forecast = createForecast(45.0, 2.0, 15.0, 28.0, 65.0);

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(req, forecast);

        assertEquals("MEDIUM", response.getRiskLevel());
        assertTrue(response.getRiskScore() >= 40 && response.getRiskScore() < 70);
        assertTrue(response.getRiskExplanation().contains("Medium Rain Risk"));
        assertFalse(response.getRecommendations().isEmpty());
    }

    @Test
    void testLowRiskClassification_ByClearSkies() {
        RiskAnalysisRequest req = createSampleRequest();
        WeatherForecastDto forecast = createForecast(10.0, 0.0, 10.0, 31.0, 45.0);

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(req, forecast);

        assertEquals("LOW", response.getRiskLevel());
        assertTrue(response.getRiskScore() < 40);
        assertTrue(response.getRiskExplanation().contains("Low Rain Risk"));
        assertFalse(response.getRecommendations().isEmpty());
        assertTrue(response.getRecommendations().get(0).contains("FAVORABLE CONDITIONS"));
    }

    @Test
    void testOutOfForecastHorizon_Handling() {
        RiskAnalysisRequest req = createSampleRequest();
        req.setHarvestDate(LocalDate.now().plusDays(25));
        WeatherForecastDto forecast = new WeatherForecastDto();
        forecast.setDataStatus("OUT_OF_HORIZON");

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(req, forecast);

        assertEquals("UNKNOWN", response.getRiskLevel());
        assertTrue(response.isForecastHorizonExceeded());
        assertEquals("OUT_OF_HORIZON", response.getDataStatus());
        assertTrue(response.getRiskExplanation().contains("14-day"));
    }

    @Test
    void testMissingWeatherData_DoesNotClassifyAsLowRisk() {
        RiskAnalysisRequest req = createSampleRequest();
        WeatherForecastDto forecast = new WeatherForecastDto();
        forecast.setDataStatus("NO_DATA");

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(req, forecast);

        assertEquals("UNKNOWN", response.getRiskLevel());
        assertEquals("NO_DATA", response.getDataStatus());
    }

    @Test
    void testTeluguVoiceAlertGeneration() {
        RiskAnalysisRequest req = createSampleRequest();
        req.setLanguage("te");
        WeatherForecastDto forecast = createForecast(75.0, 4.0, 15.0, 27.0, 75.0);

        RiskAnalysisResponse response = rainRiskService.evaluateRisk(req, forecast);

        assertEquals("HIGH", response.getRiskLevel());
        assertNotNull(response.getVoiceAlertText());
        assertTrue(response.getVoiceAlertText().contains("రైతు"));
        assertTrue(response.getVoiceAlertText().contains("అధిక ప్రమాదం"));
    }
}
