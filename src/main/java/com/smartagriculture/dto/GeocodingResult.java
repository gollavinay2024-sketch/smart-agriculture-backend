package com.smartagriculture.dto;

public class GeocodingResult {

    private String name;
    private Double latitude;
    private Double longitude;
    private String country;
    private String admin1; // State or province
    private String displayName;

    public GeocodingResult() {}

    public GeocodingResult(String name, Double latitude, Double longitude, String country, String admin1) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.admin1 = admin1;
        this.displayName = buildDisplayName();
    }

    private String buildDisplayName() {
        StringBuilder sb = new StringBuilder(name != null ? name : "");
        if (admin1 != null && !admin1.isBlank()) {
            sb.append(", ").append(admin1);
        }
        if (country != null && !country.isBlank()) {
            sb.append(", ").append(country);
        }
        return sb.toString();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getAdmin1() { return admin1; }
    public void setAdmin1(String admin1) { this.admin1 = admin1; }

    public String getDisplayName() {
        return displayName != null ? displayName : buildDisplayName();
    }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
