package com.library.demo.dto;

import java.time.LocalTime;

public class LibraryBranchDTO {
    private String branchName;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean isOpen;
    private boolean isOpenNow;
    
    public LibraryBranchDTO() {}
    
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
    
    public boolean isOpenNow() {
        return isOpenNow;
    }
    
    public void setOpenNow(boolean isOpenNow) {
        this.isOpenNow = isOpenNow;
    }
} 