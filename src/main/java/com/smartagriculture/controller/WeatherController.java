package com.smartagriculture.controller;

import com.smartagriculture.dto.GeocodingResult;
import com.smartagriculture.dto.WeatherForecastDto;
import com.smartagriculture.service.WeatherService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/geocode")
    public ResponseEntity<List<GeocodingResult>> geocode(@RequestParam String query) {
        return ResponseEntity.ok(weatherService.searchLocations(query));
    }

    @GetMapping("/forecast")
    public ResponseEntity<WeatherForecastDto> getForecast(
            @RequestParam Double lat,
            @RequestParam Double lon,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate harvestDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime harvestTime) {

        LocalDate date = harvestDate != null ? harvestDate : LocalDate.now();
        LocalTime time = harvestTime != null ? harvestTime : LocalTime.of(10, 0);

        WeatherForecastDto dto = weatherService.getForecast(lat, lon, date, time);
        return ResponseEntity.ok(dto);
    }
}
