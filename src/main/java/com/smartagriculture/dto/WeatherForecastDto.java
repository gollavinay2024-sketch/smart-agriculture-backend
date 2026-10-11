package com.smartagriculture.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class WeatherForecastDto {

    private Double latitude;
    private Double longitude;
    private String timezone;
    private HarvestSlotWeather targetSlot;
    private List<HourlyPoint> hourlyForecast = new ArrayList<>();
    private List<DailyPoint> dailyForecast = new ArrayList<>();
    private String dataStatus = "AVAILABLE"; // AVAILABLE, PARTIAL, OUT_OF_HORIZON, ERROR
    private String message;

    public static class HarvestSlotWeather {
        private LocalDateTime time;
        private Double precipitationProbability; // %
        private Double precipitationAmount; // mm
        private Double temperature; // °C
        private Double relativeHumidity; // %
        private Double windSpeed; // km/h
        private Integer weatherCode;
        private String weatherDescription;

        public HarvestSlotWeather() {}

        public LocalDateTime getTime() { return time; }
        public void setTime(LocalDateTime time) { this.time = time; }

        public Double getPrecipitationProbability() { return precipitationProbability; }
        public void setPrecipitationProbability(Double precipitationProbability) { this.precipitationProbability = precipitationProbability; }

        public Double getPrecipitationAmount() { return precipitationAmount; }
        public void setPrecipitationAmount(Double precipitationAmount) { this.precipitationAmount = precipitationAmount; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Double getRelativeHumidity() { return relativeHumidity; }
        public void setRelativeHumidity(Double relativeHumidity) { this.relativeHumidity = relativeHumidity; }

        public Double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }

        public Integer getWeatherCode() { return weatherCode; }
        public void setWeatherCode(Integer weatherCode) { this.weatherCode = weatherCode; }

        public String getWeatherDescription() { return weatherDescription; }
        public void setWeatherDescription(String weatherDescription) { this.weatherDescription = weatherDescription; }
    }

    public static class HourlyPoint {
        private String time;
        private Double precipitationProbability;
        private Double precipitation;
        private Double temperature;
        private Double windSpeed;

        public HourlyPoint() {}

        public HourlyPoint(String time, Double precipitationProbability, Double precipitation, Double temperature, Double windSpeed) {
            this.time = time;
            this.precipitationProbability = precipitationProbability;
            this.precipitation = precipitation;
            this.temperature = temperature;
            this.windSpeed = windSpeed;
        }

        public String getTime() { return time; }
        public void setTime(String time) { this.time = time; }

        public Double getPrecipitationProbability() { return precipitationProbability; }
        public void setPrecipitationProbability(Double precipitationProbability) { this.precipitationProbability = precipitationProbability; }

        public Double getPrecipitation() { return precipitation; }
        public void setPrecipitation(Double precipitation) { this.precipitation = precipitation; }

        public Double getTemperature() { return temperature; }
        public void setTemperature(Double temperature) { this.temperature = temperature; }

        public Double getWindSpeed() { return windSpeed; }
        public void setWindSpeed(Double windSpeed) { this.windSpeed = windSpeed; }
    }

    public static class DailyPoint {
        private String date;
        private Double precipitationProbabilityMax;
        private Double precipitationSum;
        private Double temperatureMax;
        private Double temperatureMin;
        private Integer weatherCode;

        public DailyPoint() {}

        public DailyPoint(String date, Double precipitationProbabilityMax, Double precipitationSum, Double temperatureMax, Double temperatureMin, Integer weatherCode) {
            this.date = date;
            this.precipitationProbabilityMax = precipitationProbabilityMax;
            this.precipitationSum = precipitationSum;
            this.temperatureMax = temperatureMax;
            this.temperatureMin = temperatureMin;
            this.weatherCode = weatherCode;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }

        public Double getPrecipitationProbabilityMax() { return precipitationProbabilityMax; }
        public void setPrecipitationProbabilityMax(Double precipitationProbabilityMax) { this.precipitationProbabilityMax = precipitationProbabilityMax; }

        public Double getPrecipitationSum() { return precipitationSum; }
        public void setPrecipitationSum(Double precipitationSum) { this.precipitationSum = precipitationSum; }

        public Double getTemperatureMax() { return temperatureMax; }
        public void setTemperatureMax(Double temperatureMax) { this.temperatureMax = temperatureMax; }

        public Double getTemperatureMin() { return temperatureMin; }
        public void setTemperatureMin(Double temperatureMin) { this.temperatureMin = temperatureMin; }

        public Integer getWeatherCode() { return weatherCode; }
        public void setWeatherCode(Integer weatherCode) { this.weatherCode = weatherCode; }
    }

    public WeatherForecastDto() {}

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getTimezone() { return timezone; }
    public void setTimezone(String timezone) { this.timezone = timezone; }

    public HarvestSlotWeather getTargetSlot() { return targetSlot; }
    public void setTargetSlot(HarvestSlotWeather targetSlot) { this.targetSlot = targetSlot; }

    public List<HourlyPoint> getHourlyForecast() { return hourlyForecast; }
    public void setHourlyForecast(List<HourlyPoint> hourlyForecast) { this.hourlyForecast = hourlyForecast; }

    public List<DailyPoint> getDailyForecast() { return dailyForecast; }
    public void setDailyForecast(List<DailyPoint> dailyForecast) { this.dailyForecast = dailyForecast; }

    public String getDataStatus() { return dataStatus; }
    public void setDataStatus(String dataStatus) { this.dataStatus = dataStatus; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
