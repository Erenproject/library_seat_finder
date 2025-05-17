package com.library.demo.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.library.demo.model.LibraryArea;

@Repository
public interface LibraryAreaRepository extends JpaRepository<LibraryArea, String> {
    
    // 查詢特定時間的圖書館座位資訊
    List<LibraryArea> findByRecordTime(LocalDateTime recordTime);
    
    // 查詢最新的圖書館座位資訊
    @Query("SELECT la FROM LibraryArea la WHERE la.recordTime = (SELECT MAX(la2.recordTime) FROM LibraryArea la2)")
    List<LibraryArea> findLatest();
    
    // 查詢指定日期的所有記錄
    @Query("SELECT la FROM LibraryArea la WHERE CAST(la.recordTime AS LocalDate) = :date")
    List<LibraryArea> findByDate(LocalDate date);
    
    // 查詢每天佔用率最高的時段
    @Query(value = "SELECT la.* FROM library_areas la " +
           "INNER JOIN (SELECT CAST(record_time AS DATE) as record_date, MAX((total_count - free_count) / NULLIF(total_count, 0)) as max_occupation " +
           "FROM library_areas " +
           "WHERE total_count > 0 " +
           "GROUP BY CAST(record_time AS DATE)) daily_max " +
           "ON CAST(la.record_time AS DATE) = daily_max.record_date " +
           "WHERE la.total_count > 0 AND (la.total_count - la.free_count) / la.total_count = daily_max.max_occupation", 
           nativeQuery = true)
    List<LibraryArea> findDailyMaxOccupation();
    
    // 查詢佔用率最高的日期 (修改使用TOP替代LIMIT)
    @Query(value = "SELECT la.* FROM library_areas la " +
           "WHERE CAST(la.record_time AS DATE) = " +
           "(SELECT TOP 1 CAST(record_time AS DATE) FROM library_areas " +
           "WHERE total_count > 0 " +
           "GROUP BY CAST(record_time AS DATE) " +
           "ORDER BY AVG((total_count - free_count) / NULLIF(total_count, 0)) DESC) " +
           "AND la.total_count > 0 " +
           "ORDER BY (la.total_count - la.free_count) / la.total_count DESC", 
           nativeQuery = true)
    List<LibraryArea> findHighestOccupationDay();
} 