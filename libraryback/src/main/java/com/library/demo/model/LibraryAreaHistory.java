package com.library.demo.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_areas_history", 
       indexes = {
           @Index(name = "idx_record_time", columnList = "recordTime"),
           @Index(name = "idx_area_id", columnList = "areaId"),
           @Index(name = "idx_area_id_record_time", columnList = "areaId,recordTime")
       })
public class LibraryAreaHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String areaId;
    private String branchName;
    private String floorName;
    private String areaName;
    private int freeCount;
    private int totalCount;
    private LocalDateTime recordTime;
    
    public LibraryAreaHistory() {}
    
    // 從LibraryArea對象創建歷史記錄對象的構造函數
    public LibraryAreaHistory(LibraryArea libraryArea) {
        this.areaId = libraryArea.getAreaId();
        this.branchName = libraryArea.getBranchName();
        this.floorName = libraryArea.getFloorName();
        this.areaName = libraryArea.getAreaName();
        this.freeCount = libraryArea.getFreeCount();
        this.totalCount = libraryArea.getTotalCount();
        this.recordTime = libraryArea.getRecordTime();
    }
    
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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