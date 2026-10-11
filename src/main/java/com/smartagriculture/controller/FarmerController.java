package com.smartagriculture.controller;

import com.smartagriculture.entity.Farmer;
import com.smartagriculture.repository.FarmerRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/farmers")
public class FarmerController {

    private final FarmerRepository farmerRepository;

    public FarmerController(FarmerRepository farmerRepository) {
        this.farmerRepository = farmerRepository;
    }

    @GetMapping
    public ResponseEntity<List<Farmer>> getAllFarmers() {
        return ResponseEntity.ok(farmerRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<Farmer> registerFarmer(@Valid @RequestBody Farmer farmer) {
        Farmer saved = farmerRepository.save(farmer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
