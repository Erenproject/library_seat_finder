package com.library.demo.model;

import java.time.LocalTime;
import java.time.ZoneId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.beans.factory.annotation.Value;

@Entity
@Table(name = "library_branches")
public class LibraryBranch {
    
    @Id
    private String branchName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isOpen;
    
    @Value("${spring.jackson.time-zone:Asia/Taipei}")
    private String timeZone;
    
    public LibraryBranch() {}
    
    public LibraryBranch(String branchName, LocalTime openTime, LocalTime closeTime, boolean isOpen) {
        this.branchName = branchName;
        this.openTime = openTime;
        this.closeTime = closeTime;
        this.isOpen = isOpen;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
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

    public boolean isOpen() {
        return isOpen;
    }

    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    
    /**
     * 判斷圖書館當前是否開放
     */
    public boolean isOpenNow() {
        LocalTime currentTime = LocalTime.now(ZoneId.of(timeZone));
        
        // 不需要parse，直接使用已有的LocalTime對象
        LocalTime openTimeValue = this.openTime;
        LocalTime closeTimeValue = this.closeTime;
        
        // 使用與ScheduledService相同的邏輯
        return !currentTime.isBefore(openTimeValue) && currentTime.isBefore(closeTimeValue);
    }
} 