package com.library.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.demo.dto.LibraryAreaDTO;
import com.library.demo.dto.OccupationStatsDTO;
import com.library.demo.service.LibraryAreaService;

@RestController
@RequestMapping("/api/library")
@CrossOrigin(origins = "*") // 允許跨域請求
public class LibraryAreaController {

    @Autowired
    private LibraryAreaService libraryAreaService;
    
    /**
     * 獲取當前圖書館座位可用性
     */
    @GetMapping("/current")
    public List<LibraryAreaDTO> getCurrentAvailability() {
        return libraryAreaService.getCurrentAvailability();
    }
    
    /**
     * 獲取統計資料：每天最高佔用率時段和最高佔用率的日期
     */
    @GetMapping("/stats")
    public OccupationStatsDTO getOccupationStats() {
        return libraryAreaService.getOccupationStats();
    }
    
    /**
     * 手動測試API調用
     */
    @GetMapping("/test-api")
    public ResponseEntity<String> testApiCall() {
        String result = libraryAreaService.testApiCall();
        if (result != null) {
            if (result.trim().startsWith("<")) {
                return ResponseEntity.ok("API返回了HTML內容而不是JSON: " + result.substring(0, Math.min(result.length(), 100)) + "...");
            } else {
                return ResponseEntity.ok("API調用成功，返回了JSON: " + result.substring(0, Math.min(result.length(), 100)) + "...");
            }
        } else {
            return ResponseEntity.status(500).body("API調用失敗，請查看服務器日誌");
        }
    }
    
    /**
     * 手動觸發資料獲取（用於測試）
     */
    @GetMapping("/fetch")
    public ResponseEntity<Map<String, Object>> manualFetch() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = libraryAreaService.manualFetchAndSave();
            response.put("message", result);
            
            // 獲取更新後的資料
            List<LibraryAreaDTO> currentData = libraryAreaService.getCurrentAvailability();
            response.put("currentData", currentData);
            response.put("dataCount", currentData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "資料獲取失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 檢視資料庫狀態（用於調試）
     */
    @GetMapping("/db-status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> status = new HashMap<>();
        try {
            List<LibraryAreaDTO> currentData = libraryAreaService.getCurrentAvailability();
            status.put("currentDataCount", currentData.size());
            status.put("currentData", currentData);
            
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            status.put("error", "獲取資料庫狀態失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(status);
        }
    }
    
    /**
     * 重置資料庫（用於調試）
     */
    @GetMapping("/reset-db")
    public ResponseEntity<Map<String, Object>> resetDatabase() {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = libraryAreaService.resetDatabase();
            response.put("message", result);
            
            // 獲取更新後的資料
            List<LibraryAreaDTO> currentData = libraryAreaService.getCurrentAvailability();
            response.put("currentData", currentData);
            response.put("dataCount", currentData.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "重置資料庫失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 獲取按分館分組的圖書館座位資料
     */
    @GetMapping("/by-branch")
    public ResponseEntity<Map<String, List<LibraryAreaDTO>>> getAvailabilityByBranch() {
        try {
            Map<String, List<LibraryAreaDTO>> groupedData = libraryAreaService.getAvailabilityByBranch();
            return ResponseEntity.ok(groupedData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
} 