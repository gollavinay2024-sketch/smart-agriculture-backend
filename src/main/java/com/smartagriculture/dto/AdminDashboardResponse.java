package com.smartagriculture.dto;

import com.smartagriculture.entity.Assessment;

import java.util.ArrayList;
import java.util.List;

public class AdminDashboardResponse {

    private long totalUsers;
    private long totalAssessments;
    private long lowRiskCount;
    private long mediumRiskCount;
    private long highRiskCount;
    private List<UserDto> recentUsers = new ArrayList<>();
    private List<Assessment> recentAssessments = new ArrayList<>();

    public AdminDashboardResponse() {}

    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }

    public long getTotalAssessments() { return totalAssessments; }
    public void setTotalAssessments(long totalAssessments) { this.totalAssessments = totalAssessments; }

    public long getLowRiskCount() { return lowRiskCount; }
    public void setLowRiskCount(long lowRiskCount) { this.lowRiskCount = lowRiskCount; }

    public long getMediumRiskCount() { return mediumRiskCount; }
    public void setMediumRiskCount(long mediumRiskCount) { this.mediumRiskCount = mediumRiskCount; }

    public long getHighRiskCount() { return highRiskCount; }
    public void setHighRiskCount(long highRiskCount) { this.highRiskCount = highRiskCount; }

    public List<UserDto> getRecentUsers() { return recentUsers; }
    public void setRecentUsers(List<UserDto> recentUsers) { this.recentUsers = recentUsers; }

    public List<Assessment> getRecentAssessments() { return recentAssessments; }
    public void setRecentAssessments(List<Assessment> recentAssessments) { this.recentAssessments = recentAssessments; }
}
