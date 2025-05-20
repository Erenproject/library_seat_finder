package com.library.demo.service;

import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
    
    /**
     * 每5分鐘檢查一次圖書館是否關閉，如果關閉則更新數據
     */
    @Scheduled(fixedRate = 300000) // 每5分鐘執行一次
    public void checkLibraryClosingAndLoadData() {
        try {
            System.out.println("開始檢查圖書館閉館狀態...");
            
            // 獲取所有分館資訊
            List<LibraryBranch> branches = libraryBranchRepository.findAll();
            
            // 檢查是否有任何開放的分館現在已經關閉
            boolean hasClosedLibraries = false;
            
            for (LibraryBranch branch : branches) {
                // 檢查分館是否標記為開放，但當前時間已經關閉
                if (branch.isOpen() && !branch.isOpenNow()) {
                    hasClosedLibraries = true;
                    System.out.println("分館 " + branch.getBranchName() + " 已閉館");
                    
                    // 更新分館狀態為關閉
                    branch.setOpen(false);
                    libraryBranchRepository.save(branch);
                }
            }
            
            // 如果有分館剛剛閉館，則觸發數據載入以記錄最終狀態
            if (hasClosedLibraries) {
                System.out.println("檢測到有分館閉館，開始載入最終數據...");
                // 這裡我們不需要啟用或禁用調度器，因為現在由ScheduledService負責
                libraryAreaService.fetchAndSaveLibraryData();
                System.out.println("閉館數據載入完成");
            }
            
        } catch (Exception e) {
            System.err.println("檢查閉館狀態時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
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
                boolean isOpenNow = branch.isOpenNow();
                
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
} 