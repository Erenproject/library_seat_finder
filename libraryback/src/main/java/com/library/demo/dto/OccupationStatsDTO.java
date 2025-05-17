package com.library.demo.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class OccupationStatsDTO {
    
    private List<LibraryAreaDTO> dailyMaxOccupations;
    private List<LibraryAreaDTO> highestOccupationDay;
    private LocalDate highestOccupationDate;
    private double highestOccupationRate;
    private LocalDateTime highestOccupationTime;
    
    public OccupationStatsDTO() {}
    
    public List<LibraryAreaDTO> getDailyMaxOccupations() {
        return dailyMaxOccupations;
    }
    
    public void setDailyMaxOccupations(List<LibraryAreaDTO> dailyMaxOccupations) {
        this.dailyMaxOccupations = dailyMaxOccupations;
    }
    
    public List<LibraryAreaDTO> getHighestOccupationDay() {
        return highestOccupationDay;
    }
    
    public void setHighestOccupationDay(List<LibraryAreaDTO> highestOccupationDay) {
        this.highestOccupationDay = highestOccupationDay;
    }
    
    public LocalDate getHighestOccupationDate() {
        return highestOccupationDate;
    }
    
    public void setHighestOccupationDate(LocalDate highestOccupationDate) {
        this.highestOccupationDate = highestOccupationDate;
    }
    
    public double getHighestOccupationRate() {
        return highestOccupationRate;
    }
    
    public void setHighestOccupationRate(double highestOccupationRate) {
        this.highestOccupationRate = highestOccupationRate;
    }
    
    public LocalDateTime getHighestOccupationTime() {
        return highestOccupationTime;
    }
    
    public void setHighestOccupationTime(LocalDateTime highestOccupationTime) {
        this.highestOccupationTime = highestOccupationTime;
    }
} 