package com.library.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_areas")
public class LibraryArea {
    
    @Id
    private String areaId;
    private String branchName;
    private String floorName;
    private String areaName;
    private int freeCount;
    private int totalCount;
    private LocalDateTime recordTime;
    
    public LibraryArea() {}
    
    public LibraryArea(String areaId, String branchName, String floorName, String areaName, 
                      int freeCount, int totalCount, LocalDateTime recordTime) {
        this.areaId = areaId;
        this.branchName = branchName;
        this.floorName = floorName;
        this.areaName = areaName;
        this.freeCount = freeCount;
        this.totalCount = totalCount;
        this.recordTime = recordTime;
    }

    public String getAreaId() {
        return areaId;
    }

    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getFloorName() {
        return floorName;
    }

    public void setFloorName(String floorName) {
        this.floorName = floorName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public int getFreeCount() {
        return freeCount;
    }

    public void setFreeCount(int freeCount) {
        this.freeCount = freeCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }
    
    public LocalDateTime getRecordTime() {
        return recordTime;
    }
    
    public void setRecordTime(LocalDateTime recordTime) {
        this.recordTime = recordTime;
    }
    
    public double getOccupationRate() {
        if (totalCount == 0) return 0;
        return (double)(totalCount - freeCount) / totalCount * 100;
    }
} 