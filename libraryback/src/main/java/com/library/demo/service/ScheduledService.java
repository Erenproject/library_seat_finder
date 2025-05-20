package com.library.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledService {

    @Value("${library.open-time}")
    private String openTime;

    @Value("${library.close-time}")
    private String closeTime;
    
    @Value("${library.weekend.open-time}")
    private String weekendOpenTime;

    @Value("${library.weekend.close-time}")
    private String weekendCloseTime;

    @Autowired
    private LibraryAreaService libraryAreaService;

    /**
     * 每分鐘執行一次，根據當前時間決定是否獲取資料
     */
    @Scheduled(cron = "0 * * * * *") // 每分鐘第0秒執行一次
    public void collectDataDuringOpenHours() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        // 根據當前是否為週末決定使用哪個時間設定
        boolean isWeekend = isWeekend(now.getDayOfWeek());
        LocalTime openTimeValue = isWeekend ? LocalTime.parse(weekendOpenTime) : LocalTime.parse(openTime);
        LocalTime closeTimeValue = isWeekend ? LocalTime.parse(weekendCloseTime) : LocalTime.parse(closeTime);
        
        // 判斷當前時間是否在開館時間內
        if (isLibraryOpen(currentTime, openTimeValue, closeTimeValue)) {
            // 如果在開館時間內，啟用定時任務並執行一次
            if (!libraryAreaService.isSchedulerEnabled()) {
                libraryAreaService.enableScheduler();
                System.out.println("圖書館開館時間，啟用定時任務");
            }
            // 每分鐘執行一次數據獲取
            libraryAreaService.fetchAndSaveLibraryData();
        } else {
            // 如果在閉館時間，禁用定時任務
            if (libraryAreaService.isSchedulerEnabled()) {
                libraryAreaService.disableScheduler();
                System.out.println("圖書館閉館時間，禁用定時任務");
            }
        }
    }
    
    /**
     * 判斷當前時間是否在開館時間內
     */
    private boolean isLibraryOpen(LocalTime currentTime, LocalTime openTime, LocalTime closeTime) {
        return !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
    }
    
    /**
     * 判斷是否為週末
     */
    private boolean isWeekend(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
} 