package com.smartagriculture;

import com.smartagriculture.service.CropRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CropRecommendationServiceTest {

    private CropRecommendationService service;

    @BeforeEach
    void setUp() {
        service = new CropRecommendationService();
    }

    @Test
    void testCottonRecommendations_HighRisk() {
        List<String> recs = service.getRecommendations("Cotton", "HIGH", "en");
        assertNotNull(recs);
        assertFalse(recs.isEmpty());
        assertTrue(recs.stream().anyMatch(r -> r.contains("PICK OPEN BOLLS IMMEDIATELY")));
    }

    @Test
    void testWheatRecommendations_HighRisk() {
        List<String> recs = service.getRecommendations("Wheat", "HIGH", "en");
        assertNotNull(recs);
        assertFalse(recs.isEmpty());
        assertTrue(recs.stream().anyMatch(r -> r.contains("SUSPEND HARVESTING")));
    }

    @Test
    void testHindiLanguageRecommendations() {
        List<String> recs = service.getRecommendations("Rice", "HIGH", "hi");
        assertNotNull(recs);
        assertFalse(recs.isEmpty());
        assertTrue(recs.stream().anyMatch(r -> r.contains("कटाई स्थगित करें")));
    }

    @Test
    void testTeluguLanguageRecommendations() {
        List<String> recs = service.getRecommendations("Rice", "HIGH", "te");
        assertNotNull(recs);
        assertFalse(recs.isEmpty());
        assertTrue(recs.stream().anyMatch(r -> r.contains("కోత వాయిదా వేయండి")));
    }
}
