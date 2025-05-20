package com.library.demo.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
@CrossOrigin(origins = "*")
public class LibraryConfigController {

    @Value("${library.open-time}")
    private String openTime;

    @Value("${library.close-time}")
    private String closeTime;
    
    @Value("${library.weekend.open-time}")
    private String weekendOpenTime;

    @Value("${library.weekend.close-time}")
    private String weekendCloseTime;
    
    /**
     * 獲取圖書館開閉館時間配置
     */
    @GetMapping("/library-hours")
    public ResponseEntity<Map<String, Object>> getLibraryHours() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("weekdayOpenTime", openTime);
        config.put("weekdayCloseTime", closeTime);
        config.put("weekendOpenTime", weekendOpenTime);
        config.put("weekendCloseTime", weekendCloseTime);
        
        return ResponseEntity.ok(config);
    }
} 