package com.library.demo.controller;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.library.demo.dto.LibraryAreaHistoryDTO;
import com.library.demo.service.LibraryAreaService;

@RestController
@RequestMapping("/api/library/history")
@CrossOrigin(origins = "*") // 允許跨域請求
public class LibraryAreaHistoryController {

    @Autowired
    private LibraryAreaService libraryAreaService;
    
    /**
     * 獲取指定日期的歷史記錄
     */
    @GetMapping("/date/{date}")
    public List<LibraryAreaHistoryDTO> getHistoryByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return libraryAreaService.getHistoryByDate(date);
    }
    
    /**
     * 獲取指定日期範圍的歷史記錄
     */
    @GetMapping("/range")
    public List<LibraryAreaHistoryDTO> getHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return libraryAreaService.getHistoryByDateRange(startDate, endDate);
    }
    
    /**
     * 獲取特定區域在指定日期的歷史記錄
     */
    @GetMapping("/area/{areaId}/date/{date}")
    public List<LibraryAreaHistoryDTO> getHistoryByAreaAndDate(
            @PathVariable String areaId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return libraryAreaService.getHistoryByAreaAndDate(areaId, date);
    }
    
    /**
     * 獲取特定區域在指定日期最繁忙的時間段
     */
    @GetMapping("/area/{areaId}/date/{date}/busiest-hours")
    public ResponseEntity<Map<String, Object>> getBusiestHoursByAreaAndDate(
            @PathVariable String areaId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Object[]> results = libraryAreaService.getBusiestHoursByAreaAndDate(areaId, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("areaId", areaId);
        response.put("date", date);
        response.put("busiestHours", results);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 獲取指定日期每個區域的平均使用率
     */
    @GetMapping("/date/{date}/average-occupation")
    public ResponseEntity<Map<String, Object>> getAverageOccupationByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Object[]> results = libraryAreaService.getAverageOccupationByDate(date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("date", date);
        response.put("areaStats", results);
        
        return ResponseEntity.ok(response);
    }
} 