package com.smartagriculture.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "farmers")
public class Farmer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Farmer name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String phoneNumber;

    @Column(length = 10)
    private String preferredLanguage = "en";

    @Column(length = 150)
    private String villageLocation;

    private Double latitude;
    private Double longitude;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Farmer() {}

    public Farmer(String name, String phoneNumber, String preferredLanguage, String villageLocation) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.preferredLanguage = preferredLanguage;
        this.villageLocation = villageLocation;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public String getVillageLocation() { return villageLocation; }
    public void setVillageLocation(String villageLocation) { this.villageLocation = villageLocation; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
