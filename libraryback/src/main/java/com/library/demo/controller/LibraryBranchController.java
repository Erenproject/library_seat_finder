package com.library.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.demo.dto.LibraryBranchDTO;
import com.library.demo.service.LibraryBranchService;
import com.library.demo.service.ScheduledService;

@RestController
@RequestMapping("/api/branch")
@CrossOrigin(origins = "*")
public class LibraryBranchController {

    @Autowired
    private LibraryBranchService libraryBranchService;
    
    @Autowired
    private ScheduledService scheduledService;
    
    /**
     * 獲取所有分館信息
     */
    @GetMapping("/all")
    public List<LibraryBranchDTO> getAllBranches() {
        return libraryBranchService.getAllBranches();
    }
    
    /**
     * 更新或添加分館信息
     */
    @PostMapping("/save")
    public ResponseEntity<LibraryBranchDTO> saveBranch(@RequestBody LibraryBranchDTO branchDTO) {
        try {
            LibraryBranchDTO savedBranch = libraryBranchService.saveBranch(branchDTO);
            return ResponseEntity.ok(savedBranch);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    
    /**
     * 手動檢查閉館狀態並更新數據
     */
    @GetMapping("/check-closing")
    public ResponseEntity<Map<String, Object>> checkLibraryClosing() {
        Map<String, Object> response = new HashMap<>();
        try {
            scheduledService.checkForClosingTimeUpdate();
            response.put("message", "成功檢查閉館狀態並更新數據");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "檢查閉館狀態失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    /**
     * 手動更新分館開放狀態
     */
    @GetMapping("/update-status")
    public ResponseEntity<Map<String, Object>> updateBranchStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            libraryBranchService.updateBranchOpenStatus();
            response.put("message", "成功更新分館開放狀態");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "更新分館開放狀態失敗: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 