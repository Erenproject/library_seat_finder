package com.library.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.library.demo.model.LibraryAreaHistory;

@Repository
public interface LibraryAreaHistoryRepository extends JpaRepository<LibraryAreaHistory, Long> {
    
    // 查詢指定日期的所有歷史記錄
    @Query("SELECT lah FROM LibraryAreaHistory lah WHERE CAST(lah.recordTime AS LocalDate) = :date")
    List<LibraryAreaHistory> findByDate(LocalDate date);
    
    // 查詢指定時間範圍的歷史記錄
    List<LibraryAreaHistory> findByRecordTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    // 查詢指定區域在特定日期的歷史記錄
    @Query("SELECT lah FROM LibraryAreaHistory lah WHERE lah.areaId = :areaId AND CAST(lah.recordTime AS LocalDate) = :date")
    List<LibraryAreaHistory> findByAreaIdAndDate(String areaId, LocalDate date);
    
    // 查詢特定區域在特定時間範圍的使用率變化
    @Query("SELECT lah FROM LibraryAreaHistory lah WHERE lah.areaId = :areaId AND lah.recordTime BETWEEN :startTime AND :endTime ORDER BY lah.recordTime")
    List<LibraryAreaHistory> findOccupationRateChangeByAreaId(String areaId, LocalDateTime startTime, LocalDateTime endTime);
    
    // 查詢特定日期每個區域的平均使用率
    @Query("SELECT lah.areaId, lah.areaName, lah.branchName, lah.floorName, AVG((lah.totalCount - lah.freeCount) * 100.0 / CASE WHEN lah.totalCount = 0 THEN 1 ELSE lah.totalCount END) as avgOccupation " +
           "FROM LibraryAreaHistory lah WHERE CAST(lah.recordTime AS LocalDate) = :date " +
           "AND lah.totalCount > 0 " +
           "GROUP BY lah.areaId, lah.areaName, lah.branchName, lah.floorName " +
           "ORDER BY avgOccupation DESC")
    List<Object[]> findAverageOccupationByDateGroupByArea(LocalDate date);
    
    // 查詢特定區域最繁忙的時間段（按小時分組）
    @Query(value = "SELECT DATEPART(HOUR, lah.record_time) as hour, AVG((lah.total_count - lah.free_count) * 100.0 / " +
           "CASE WHEN lah.total_count = 0 THEN 1 ELSE lah.total_count END) as avgOccupation " +
           "FROM library_areas_history lah WHERE lah.area_id = :areaId AND CAST(lah.record_time AS DATE) = :date " +
           "AND lah.total_count > 0 " +
           "GROUP BY DATEPART(HOUR, lah.record_time) " +
           "ORDER BY avgOccupation DESC", nativeQuery = true)
    List<Object[]> findBusiestHoursByAreaAndDate(String areaId, LocalDate date);
    
    // 查詢特定分館最繁忙的時間段（按小時分組）
    @Query(value = "SELECT DATEPART(HOUR, lah.record_time) as hour, AVG((lah.total_count - lah.free_count) * 100.0 / " +
           "CASE WHEN lah.total_count = 0 THEN 1 ELSE lah.total_count END) as avgOccupation " +
           "FROM library_areas_history lah WHERE lah.branch_name = :branchName AND CAST(lah.record_time AS DATE) = :date " +
           "AND lah.total_count > 0 " +
           "GROUP BY DATEPART(HOUR, lah.record_time) " +
           "ORDER BY hour ASC", nativeQuery = true)
    List<Object[]> findBusiestHoursByBranchAndDate(String branchName, LocalDate date);
    
    // 計算特定時間之前的記錄數量
    long countByRecordTimeBefore(LocalDateTime dateTime);
    
    // 刪除特定時間之前的記錄
    @Modifying
    @Transactional
    @Query("DELETE FROM LibraryAreaHistory lah WHERE lah.recordTime < :dateTime")
    int deleteByRecordTimeBefore(LocalDateTime dateTime);
} 