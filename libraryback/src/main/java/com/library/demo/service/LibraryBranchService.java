package com.library.demo.service;

import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.library.demo.dto.LibraryBranchDTO;
import com.library.demo.model.LibraryBranch;
import com.library.demo.repository.LibraryBranchRepository;
import com.library.demo.service.LibraryAreaService;

@Service
public class LibraryBranchService {

    @Autowired
    private LibraryBranchRepository libraryBranchRepository;
    
    @Autowired
    private LibraryAreaService libraryAreaService;
    
    @Value("${library.close-time}")
    private String closeTime;
    
    @Value("${library.weekend.close-time}")
    private String weekendCloseTime;
    
    @Value("${spring.jackson.time-zone:Asia/Taipei}")
    private String timeZone;
    
    /**
     * 獲取所有分館資訊
     */
    public List<LibraryBranchDTO> getAllBranches() {
        List<LibraryBranch> branches = libraryBranchRepository.findAll();
        return branches.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新分館開放狀態
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小時執行一次
    public void updateBranchOpenStatus() {
        try {
            List<LibraryBranch> branches = libraryBranchRepository.findAll();
            
            for (LibraryBranch branch : branches) {
                boolean wasOpen = branch.isOpen();
                boolean isOpenNow = isLibraryOpenNow(branch);
                
                // 如果開放狀態發生變化，更新記錄
                if (wasOpen != isOpenNow) {
                    branch.setOpen(isOpenNow);
                    libraryBranchRepository.save(branch);
                    System.out.println("更新分館 " + branch.getBranchName() + " 開放狀態為: " + (isOpenNow ? "開放" : "關閉"));
                }
            }
        } catch (Exception e) {
            System.err.println("更新分館開放狀態時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 將實體轉換為DTO
     */
    private LibraryBranchDTO convertToDTO(LibraryBranch branch) {
        LibraryBranchDTO dto = new LibraryBranchDTO();
        dto.setBranchName(branch.getBranchName());
        dto.setOpenTime(branch.getOpenTime());
        dto.setCloseTime(branch.getCloseTime());
        dto.setOpen(branch.isOpen());
        dto.setOpenNow(branch.isOpenNow());
        return dto;
    }
    
    /**
     * 添加或更新分館資訊
     */
    public LibraryBranchDTO saveBranch(LibraryBranchDTO branchDTO) {
        LibraryBranch branch = new LibraryBranch();
        branch.setBranchName(branchDTO.getBranchName());
        branch.setOpenTime(branchDTO.getOpenTime());
        branch.setCloseTime(branchDTO.getCloseTime());
        branch.setOpen(branchDTO.isOpen());
        
        branch = libraryBranchRepository.save(branch);
        return convertToDTO(branch);
    }

    public boolean isLibraryOpenNow(LibraryBranch branch) {
        LocalTime currentTime = LocalTime.now(ZoneId.of(timeZone));
        return !currentTime.isBefore(branch.getOpenTime()) && 
               currentTime.isBefore(branch.getCloseTime());
    }
} 