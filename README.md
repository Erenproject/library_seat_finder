# 臺北市圖書館座位查詢系統

這是一個能夠查詢臺北市圖書館各分館座位使用情況的系統。系統每分鐘自動從臺北市圖書館API獲取最新座位資料，並提供歷史數據統計分析功能。

## 系統功能

1. **即時座位查詢**：
   - 顯示各分館當前的座位可用情況
   - 包括區域編號、分館名稱、樓層、區域名稱、剩餘座位數、總座位數和佔用率

2. **統計分析功能**：
   - 查看每天佔用率最高的時段
   - 查看歷史上佔用率最高的日期
   - 通過圖表直觀展示佔用率趨勢

## 技術架構

### 前端
- Vue.js 3
- Chart.js (資料視覺化)
- Vite (構建工具)

### 後端
- Spring Boot
- Spring Data JPA
- Microsoft SQL Server (資料庫)
- RESTful API

## 系統架構圖

前端 → 後端 → 臺北市圖書館API
  ↑        ↓
  └── 資料庫 ←┘

## 如何運行

### 後端設置
1. 確保已安裝Java 17或更高版本
2. 修改 `libraryback/src/main/resources/application.properties` 配置文件中的資料庫連接參數
3. 執行以下命令：
```bash
cd libraryback
./mvnw spring-boot:run
```

### 前端設置
1. 確保已安裝Node.js
2. 執行以下命令：
```bash
cd libraryseatquery
npm install
npm run dev
```

## API文檔

### 後端API

- **GET /api/library/current**：獲取當前座位可用性
- **GET /api/library/stats**：獲取統計數據

## 開發人員

- 您的名字

## 資料來源

本系統使用臺北市圖書館開放API：https://seat.tpml.edu.tw/sm/service/getAllArea 