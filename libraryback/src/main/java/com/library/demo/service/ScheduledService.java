package com.library.demo.service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
    
    @Value("${spring.jackson.time-zone:Asia/Taipei}")
    private String timeZone;

    @Autowired
    private LibraryAreaService libraryAreaService;

    /**
     * 每分鐘執行一次，根據當前時間決定是否獲取資料
     */
    @Scheduled(cron = "0 * * * * *") // 每分鐘第0秒執行一次
    public void collectDataDuringOpenHours() {
        // 使用設定的時區獲取當前時間
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
        LocalDateTime localNow = now.toLocalDateTime();
        LocalTime currentTime = localNow.toLocalTime();
        
        // 輸出現在的時間和時區進行調試
        System.out.println("現在時間（" + timeZone + "）：" + localNow);
        
        // 根據當前是否為週日或週一決定使用哪個時間設定
        boolean isSundayOrMonday = isSundayOrMonday(localNow.getDayOfWeek());
        LocalTime openTimeValue = isSundayOrMonday ? LocalTime.parse(weekendOpenTime) : LocalTime.parse(openTime);
        LocalTime closeTimeValue = isSundayOrMonday ? LocalTime.parse(weekendCloseTime) : LocalTime.parse(closeTime);
        
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
     * 每分鐘檢查是否接近閉館時間，如果是則執行閉館前最後一次數據更新
     * 設定為閉館前5分鐘執行
     */
    @Scheduled(cron = "0 * * * * *") // 每分鐘執行一次檢查
    public void checkForClosingTimeUpdate() {
        // 使用設定的時區獲取當前時間
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(timeZone));
        LocalDateTime localNow = now.toLocalDateTime();
        LocalTime currentTime = localNow.toLocalTime();
        
        // 根據當前是否為週日或週一決定使用哪個閉館時間
        boolean isSundayOrMonday = isSundayOrMonday(localNow.getDayOfWeek());
        LocalTime closeTimeValue = isSundayOrMonday ? LocalTime.parse(weekendCloseTime) : LocalTime.parse(closeTime);
        
        // 計算閉館前5分鐘的時間
        LocalTime fiveMinutesBeforeClosing = closeTimeValue.minusMinutes(5);
        
        // 如果當前時間在閉館前5分鐘內的特定一分鐘（例如19:55-19:56）
        System.out.println("ScheduledService - 檢查閉館時間: " + currentTime + ", 閉館時間: " + closeTimeValue);
        if (currentTime.compareTo(fiveMinutesBeforeClosing) >= 0 && 
            currentTime.compareTo(fiveMinutesBeforeClosing.plusMinutes(1)) < 0) {
            
            System.out.println("ScheduledService - 將執行閉館前最後數據更新");
            
            // 創建閉館時間的完整日期時間對象
            LocalDateTime closingDateTime = LocalDateTime.of(localNow.toLocalDate(), closeTimeValue);
            
            // 使用閉館時間作為時間戳記執行最後一次數據更新
            libraryAreaService.fetchAndSaveWithCustomTime(closingDateTime);
            
            System.out.println("閉館前最後數據更新完成，時間戳設為閉館時間: " + closingDateTime);
        }
    }
    
    /**
     * 判斷當前時間是否在開館時間內
     */
    private boolean isLibraryOpen(LocalTime currentTime, LocalTime openTime, LocalTime closeTime) {
        return !currentTime.isBefore(openTime) && currentTime.isBefore(closeTime);
    }
    
    /**
     * 判斷是否為週日或週一
     */
    private boolean isSundayOrMonday(DayOfWeek dayOfWeek) {
        return dayOfWeek == DayOfWeek.SUNDAY || dayOfWeek == DayOfWeek.MONDAY;
    }
} 