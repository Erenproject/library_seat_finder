package com.library.demo.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public class LibraryAreaDTO {
    private String areaId;
    private String branchName;
    private String floorName;
    private String areaName;
    private int freeCount;
    private int totalCount;
    private LocalDateTime recordTime;
    private double occupationRate;
    private boolean isLibraryOpen;
    private LocalTime openTime;
    private LocalTime closeTime;
    
    public LibraryAreaDTO() {}
    
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
        return occupationRate;
    }
    
    public void setOccupationRate(double occupationRate) {
        this.occupationRate = occupationRate;
    }
    
    public boolean isLibraryOpen() {
        return isLibraryOpen;
    }
    
    public void setLibraryOpen(boolean isLibraryOpen) {
        this.isLibraryOpen = isLibraryOpen;
    }
    
    public LocalTime getOpenTime() {
        return openTime;
    }
    
    public void setOpenTime(LocalTime openTime) {
        this.openTime = openTime;
    }
    
    public LocalTime getCloseTime() {
        return closeTime;
    }
    
    public void setCloseTime(LocalTime closeTime) {
        this.closeTime = closeTime;
    }
} 