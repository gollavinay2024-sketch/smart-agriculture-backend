package com.smartagriculture.dto;

import com.smartagriculture.entity.Assessment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardSummaryResponse {

    private long totalAssessments;
    private long lowRiskCount;
    private long mediumRiskCount;
    private long highRiskCount;
    private Map<String, Long> cropDistribution = new HashMap<>();
    private List<Assessment> recentAssessments = new ArrayList<>();

    public DashboardSummaryResponse() {}

    public long getTotalAssessments() { return totalAssessments; }
    public void setTotalAssessments(long totalAssessments) { this.totalAssessments = totalAssessments; }

    public long getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(long lowRiskCount) { this.lowRiskCount = lowRiskCount; }

    public long getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(long mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }

    public long getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(long highRiskCount) { this.highRiskCount = highRiskCount; }

    public Map<String, Long> getCropDistribution() { return cropDistribution; }
    public void setCropDistribution(Map<String, Long> cropDistribution) { this.cropDistribution = cropDistribution; }

    public List<Assessment> getRecentAssessments() { return recentAssessments; }
    public void setRecentAssessments(List<Assessment> recentAssessments) { this.recentAssessments = recentAssessments; }
}
