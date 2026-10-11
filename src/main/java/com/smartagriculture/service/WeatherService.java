package com.smartagriculture.service;

import com.smartagriculture.dto.GeocodingResult;
import com.smartagriculture.dto.WeatherForecastDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class WeatherService {

    private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

    private final RestTemplate restTemplate;

    @Value("${app.weather.forecast-api-url:https://api.open-meteo.com/v1/forecast}")
    private String forecastApiUrl;

    @Value("${app.weather.geocoding-api-url:https://geocoding-api.open-meteo.com/v1/search}")
    private String geocodingApiUrl;

    public WeatherService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Search coordinates using Open-Meteo Geocoding API
     */
    public List<GeocodingResult> searchLocations(String query) {
        if (query == null || query.trim().length() < 2) {
            return Collections.emptyList();
        }

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(geocodingApiUrl)
                    .queryParam("name", query.trim())
                    .queryParam("count", 5)
                    .queryParam("language", "en")
                    .queryParam("format", "json")
                    .toUriString();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null || !response.containsKey("results")) {
                return Collections.emptyList();
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
            List<GeocodingResult> list = new ArrayList<>();

            for (Map<String, Object> item : results) {
                String name = (String) item.get("name");
                Double lat = item.get("latitude") instanceof Number ? ((Number) item.get("latitude")).doubleValue() : null;
                Double lon = item.get("longitude") instanceof Number ? ((Number) item.get("longitude")).doubleValue() : null;
                String country = (String) item.get("country");
                String admin1 = (String) item.get("admin1");

                if (lat != null && lon != null) {
                    list.add(new GeocodingResult(name, lat, lon, country, admin1));
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Geocoding API error for query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Fetches real weather forecast from Open-Meteo and locates target slot
     */
    public WeatherForecastDto getForecast(Double latitude, Double longitude, LocalDate harvestDate, LocalTime harvestTime) {
        WeatherForecastDto dto = new WeatherForecastDto();
        dto.setLatitude(latitude);
        dto.setLongitude(longitude);

        LocalDate today = LocalDate.now();
        if (harvestDate.isBefore(today)) {
            dto.setDataStatus("INVALID_DATE");
            dto.setMessage("Planned harvest date cannot be in the past.");
            return dto;
        }

        long daysAhead = ChronoUnit.DAYS.between(today, harvestDate);
        if (daysAhead > 14) {
            dto.setDataStatus("OUT_OF_HORIZON");
            dto.setMessage("Harvest date is beyond Open-Meteo's standard 14-day forecast horizon. Reliable atmospheric modeling is not available beyond 14 days.");
            return dto;
        }

        try {
            String uri = UriComponentsBuilder.fromHttpUrl(forecastApiUrl)
                    .queryParam("latitude", latitude)
                    .queryParam("longitude", longitude)
                    .queryParam("hourly", "temperature_2m,relative_humidity_2m,precipitation_probability,precipitation,weather_code,wind_speed_10m")
                    .queryParam("daily", "weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum,precipitation_probability_max")
                    .queryParam("timezone", "auto")
                    .queryParam("forecast_days", 14)
                    .toUriString();

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
            if (response == null) {
                dto.setDataStatus("ERROR");
                dto.setMessage("No response received from weather forecast service.");
                return dto;
            }

            dto.setTimezone((String) response.get("timezone"));

            // Parse Hourly data
            @SuppressWarnings("unchecked")
            Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
            if (hourly != null) {
                parseHourlyAndFindTargetSlot(dto, hourly, harvestDate, harvestTime);
            }

            // Parse Daily data for 7/14 day overview
            @SuppressWarnings("unchecked")
            Map<String, Object> daily = (Map<String, Object>) response.get("daily");
            if (daily != null) {
                parseDaily(dto, daily);
            }

            if (dto.getTargetSlot() != null) {
                dto.setDataStatus("AVAILABLE");
            } else if (!"OUT_OF_HORIZON".equals(dto.getDataStatus())) {
                dto.setDataStatus("PARTIAL");
                dto.setMessage("Target hour was outside hourly range; falling back to nearest forecast point.");
            }

            return dto;
        } catch (Exception e) {
            log.error("Failed to fetch weather forecast for lat={}, lon={}: {}", latitude, longitude, e.getMessage());
            dto.setDataStatus("ERROR");
            dto.setMessage("Could not retrieve real-time weather forecast: " + e.getMessage());
            return dto;
        }
    }

    private void parseHourlyAndFindTargetSlot(WeatherForecastDto dto, Map<String, Object> hourly, LocalDate harvestDate, LocalTime harvestTime) {
        @SuppressWarnings("unchecked")
        List<String> times = (List<String>) hourly.get("time");
        @SuppressWarnings("unchecked")
        List<Number> precipProbs = (List<Number>) hourly.get("precipitation_probability");
        @SuppressWarnings("unchecked")
        List<Number> precips = (List<Number>) hourly.get("precipitation");
        @SuppressWarnings("unchecked")
        List<Number> temps = (List<Number>) hourly.get("temperature_2m");
        @SuppressWarnings("unchecked")
        List<Number> humidities = (List<Number>) hourly.get("relative_humidity_2m");
        @SuppressWarnings("unchecked")
        List<Number> windSpeeds = (List<Number>) hourly.get("wind_speed_10m");
        @SuppressWarnings("unchecked")
        List<Number> weatherCodes = (List<Number>) hourly.get("weather_code");

        if (times == null || times.isEmpty()) return;

        LocalDateTime targetDateTime = LocalDateTime.of(harvestDate, harvestTime != null ? harvestTime : LocalTime.of(10, 0));
        DateTimeFormatter isoFormat = DateTimeFormatter.ISO_DATE_TIME;

        int closestIndex = -1;
        long minDifferenceMinutes = Long.MAX_VALUE;

        List<WeatherForecastDto.HourlyPoint> hourlyList = new ArrayList<>();

        for (int i = 0; i < times.size(); i++) {
            String timeStr = times.get(i);
            LocalDateTime slotTime;
            try {
                slotTime = LocalDateTime.parse(timeStr, isoFormat);
            } catch (Exception ex) {
                continue;
            }

            Double pProb = (precipProbs != null && i < precipProbs.size() && precipProbs.get(i) != null) ? precipProbs.get(i).doubleValue() : null;
            Double precip = (precips != null && i < precips.size() && precips.get(i) != null) ? precips.get(i).doubleValue() : null;
            Double temp = (temps != null && i < temps.size() && temps.get(i) != null) ? temps.get(i).doubleValue() : null;
            Double wind = (windSpeeds != null && i < windSpeeds.size() && windSpeeds.get(i) != null) ? windSpeeds.get(i).doubleValue() : null;

            // Sample hourly points for frontend display (e.g., first 48 hours)
            if (i < 48) {
                hourlyList.add(new WeatherForecastDto.HourlyPoint(timeStr, pProb, precip, temp, wind));
            }

            long diff = Math.abs(ChronoUnit.MINUTES.between(targetDateTime, slotTime));
            if (diff < minDifferenceMinutes) {
                minDifferenceMinutes = diff;
                closestIndex = i;
            }
        }

        dto.setHourlyForecast(hourlyList);

        if (closestIndex != -1 && minDifferenceMinutes <= 1440) { // Within 24h
            WeatherForecastDto.HarvestSlotWeather slot = new WeatherForecastDto.HarvestSlotWeather();
            try {
                slot.setTime(LocalDateTime.parse(times.get(closestIndex), isoFormat));
            } catch (Exception ignored) {}

            slot.setPrecipitationProbability(precipProbs != null && closestIndex < precipProbs.size() && precipProbs.get(closestIndex) != null ? precipProbs.get(closestIndex).doubleValue() : null);
            slot.setPrecipitationAmount(precips != null && closestIndex < precips.size() && precips.get(closestIndex) != null ? precips.get(closestIndex).doubleValue() : null);
            slot.setTemperature(temps != null && closestIndex < temps.size() && temps.get(closestIndex) != null ? temps.get(closestIndex).doubleValue() : null);
            slot.setRelativeHumidity(humidities != null && closestIndex < humidities.size() && humidities.get(closestIndex) != null ? humidities.get(closestIndex).doubleValue() : null);
            slot.setWindSpeed(windSpeeds != null && closestIndex < windSpeeds.size() && windSpeeds.get(closestIndex) != null ? windSpeeds.get(closestIndex).doubleValue() : null);
            
            Integer code = weatherCodes != null && closestIndex < weatherCodes.size() && weatherCodes.get(closestIndex) != null ? weatherCodes.get(closestIndex).intValue() : null;
            slot.setWeatherCode(code);
            slot.setWeatherDescription(describeWeatherCode(code));

            dto.setTargetSlot(slot);
        }
    }

    private void parseDaily(WeatherForecastDto dto, Map<String, Object> daily) {
        @SuppressWarnings("unchecked")
        List<String> dates = (List<String>) daily.get("time");
        @SuppressWarnings("unchecked")
        List<Number> maxProbs = (List<Number>) daily.get("precipitation_probability_max");
        @SuppressWarnings("unchecked")
        List<Number> precipSums = (List<Number>) daily.get("precipitation_sum");
        @SuppressWarnings("unchecked")
        List<Number> tempMax = (List<Number>) daily.get("temperature_2m_max");
        @SuppressWarnings("unchecked")
        List<Number> tempMin = (List<Number>) daily.get("temperature_2m_min");
        @SuppressWarnings("unchecked")
        List<Number> codes = (List<Number>) daily.get("weather_code");

        if (dates == null) return;

        List<WeatherForecastDto.DailyPoint> dailyList = new ArrayList<>();
        for (int i = 0; i < dates.size(); i++) {
            String date = dates.get(i);
            Double pProb = (maxProbs != null && i < maxProbs.size() && maxProbs.get(i) != null) ? maxProbs.get(i).doubleValue() : null;
            Double pSum = (precipSums != null && i < precipSums.size() && precipSums.get(i) != null) ? precipSums.get(i).doubleValue() : null;
            Double tMax = (tempMax != null && i < tempMax.size() && tempMax.get(i) != null) ? tempMax.get(i).doubleValue() : null;
            Double tMin = (tempMin != null && i < tempMin.size() && tempMin.get(i) != null) ? tempMin.get(i).doubleValue() : null;
            Integer code = (codes != null && i < codes.size() && codes.get(i) != null) ? codes.get(i).intValue() : null;

            dailyList.add(new WeatherForecastDto.DailyPoint(date, pProb, pSum, tMax, tMin, code));
        }
        dto.setDailyForecast(dailyList);
    }

    public String describeWeatherCode(Integer code) {
        if (code == null) return "Unknown";
        return switch (code) {
            case 0 -> "Clear Sky";
            case 1 -> "Mainly Clear";
            case 2 -> "Partly Cloudy";
            case 3 -> "Overcast";
            case 45, 48 -> "Foggy";
            case 51, 53, 55 -> "Drizzle";
            case 56, 57 -> "Freezing Drizzle";
            case 61 -> "Slight Rain";
            case 63 -> "Moderate Rain";
            case 65 -> "Heavy Rain";
            case 66, 67 -> "Freezing Rain";
            case 71, 73, 75 -> "Snow Fall";
            case 77 -> "Snow Grains";
            case 80, 81, 82 -> "Rain Showers";
            case 85, 86 -> "Snow Showers";
            case 95 -> "Thunderstorm";
            case 96, 99 -> "Thunderstorm with Hail";
            default -> "Cloudy with variable conditions";
        };
    }
}
