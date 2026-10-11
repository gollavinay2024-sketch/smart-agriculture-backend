package com.smartagriculture.repository;

import com.smartagriculture.entity.Assessment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    // Global queries (Admin / Open fallback)
    List<Assessment> findAllByOrderByCreatedAtDesc();

    List<Assessment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Assessment> findByCropTypeIgnoreCaseOrderByCreatedAtDesc(String cropType);

    List<Assessment> findByRiskLevelIgnoreCaseOrderByCreatedAtDesc(String riskLevel);

    List<Assessment> findByCropTypeIgnoreCaseAndRiskLevelIgnoreCaseOrderByCreatedAtDesc(String cropType, String riskLevel);

    long countByRiskLevelIgnoreCase(String riskLevel);

    @Query("SELECT a.cropType, COUNT(a) FROM Assessment a GROUP BY a.cropType")
    List<Object[]> countGroupByCropType();

    @Query("SELECT a.riskLevel, COUNT(a) FROM Assessment a GROUP BY a.riskLevel")
    List<Object[]> countGroupByRiskLevel();

    // User-scoped queries (Individual farmer privacy)
    List<Assessment> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Assessment> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<Assessment> findByUserIdAndCropTypeIgnoreCaseOrderByCreatedAtDesc(Long userId, String cropType);

    List<Assessment> findByUserIdAndRiskLevelIgnoreCaseOrderByCreatedAtDesc(Long userId, String riskLevel);

    List<Assessment> findByUserIdAndCropTypeIgnoreCaseAndRiskLevelIgnoreCaseOrderByCreatedAtDesc(Long userId, String cropType, String riskLevel);

    long countByUserId(Long userId);

    long countByUserIdAndRiskLevelIgnoreCase(Long userId, String riskLevel);

    @Query("SELECT a.cropType, COUNT(a) FROM Assessment a WHERE a.userId = :userId GROUP BY a.cropType")
    List<Object[]> countGroupByCropTypeForUser(@Param("userId") Long userId);
}
