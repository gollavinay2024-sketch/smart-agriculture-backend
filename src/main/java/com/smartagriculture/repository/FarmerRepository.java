package com.smartagriculture.repository;

import com.smartagriculture.entity.Farmer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FarmerRepository extends JpaRepository<Farmer, Long> {
    Optional<Farmer> findByName(String name);
    Optional<Farmer> findByPhoneNumber(String phoneNumber);
}
