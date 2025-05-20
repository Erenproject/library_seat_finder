package com.library.demo.model;

import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "library_branches")
public class LibraryBranch {
    
    @Id
    private String branchName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isOpen;
    
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
    
    // 判斷當前時間分館是否開放
    public boolean isOpenNow() {
        LocalTime now = LocalTime.now();
        return isOpen && now.isAfter(openTime) && now.isBefore(closeTime);
    }
} 