<script setup>
import { ref, onMounted, computed } from 'vue'
import ChartComponent from '../components/ChartComponent.vue'
import PeakHoursComponent from '../components/PeakHoursComponent.vue'
import axios from 'axios'

// 設定 axios 基礎 URL

const apiBaseUrl = 'http://localhost:8080/api'
// const apiBaseUrl = 'http://35.87.154.178:8080/api'

const libraryApiUrl = `${apiBaseUrl}/library`
const axiosInstance = axios.create({
  baseURL: libraryApiUrl,
  timeout: 10000
})

const areas = ref([])
const groupedAreas = ref({})
const stats = ref(null)
const error = ref('')
const loading = ref(false)
const activeTab = ref('current')
const viewMode = ref('group') // 'group' 或 'table' 檢視模式

// 圖書館開閉館時間
const libraryHours = ref({
  weekdayOpenTime: '',
  weekdayCloseTime: '',
  weekendOpenTime: '',
  weekendCloseTime: ''
})
const isLibraryOpenNow = ref(false)

// 熱門時段相關
const peakHoursData = ref([])
const selectedArea = ref(null)
const selectedBranch = ref(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const showPeakHours = ref(false)
const peakHoursType = ref('area') // 'area' 或 'branch'

// 系統當前時間
const currentSystemTime = ref('')

// 更新系統時間
const updateSystemTime = () => {
  const now = new Date()
  const options = { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit', 
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
    timeZone: 'Asia/Taipei'
  }
  currentSystemTime.value = now.toLocaleString('zh-TW', options).replace(/\//g, '-')
}

// 獲取當前座位可用性（表格檢視）
const fetchCurrentData = async () => {
  loading.value = true
  try {
    const response = await axiosInstance.get('/current')
    areas.value = response.data
  } catch (err) {
    error.value = '資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 獲取按分館分組的座位資料
const fetchGroupedData = async () => {
  loading.value = true
  try {
    const response = await axiosInstance.get('/by-branch')
    groupedAreas.value = response.data
    // 輸出後端返回的時間格式，用於調試
    if (Object.keys(response.data).length > 0) {
      const firstBranch = Object.keys(response.data)[0]
      if (response.data[firstBranch] && response.data[firstBranch].length > 0) {
        console.log('後端返回的原始時間:', response.data[firstBranch][0].recordTime)
        console.log('解析為JS Date對象:', new Date(response.data[firstBranch][0].recordTime))
        console.log('解析後的UTC時間:', new Date(response.data[firstBranch][0].recordTime).toUTCString())
        console.log('解析後的本地時間:', new Date(response.data[firstBranch][0].recordTime).toLocaleString())
      }
    }
  } catch (err) {
    error.value = '資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 獲取圖書館營業時間
const fetchLibraryHours = async () => {
  try {
    const response = await axios.get(`${apiBaseUrl}/config/library-hours`)
    console.log('API返回的營業時間數據:', response.data)
    libraryHours.value = response.data
    checkLibraryOpenStatus()
  } catch (err) {
    console.error('獲取圖書館營業時間失敗:', err)
  }
}

// 檢查圖書館是否開放中
const checkLibraryOpenStatus = () => {
  const now = new Date()
  const currentTime = now.getHours() * 60 + now.getMinutes()
  
  // 判斷是否為週日或週一
  const dayOfWeek = now.getDay() // 0 是週日，1 是週一，2 是週二，以此類推
  const isSundayOrMonday = dayOfWeek === 0 || dayOfWeek === 1
  
  // 選擇對應的開閉館時間
  const openTimeStr = isSundayOrMonday ? libraryHours.value.weekendOpenTime : libraryHours.value.weekdayOpenTime
  const closeTimeStr = isSundayOrMonday ? libraryHours.value.weekendCloseTime : libraryHours.value.weekdayCloseTime
  
  // 轉換時間為分鐘
  const openTimeParts = openTimeStr.split(':')
  const closeTimeParts = closeTimeStr.split(':')
  
  const openTimeMinutes = parseInt(openTimeParts[0]) * 60 + parseInt(openTimeParts[1])
  const closeTimeMinutes = parseInt(closeTimeParts[0]) * 60 + parseInt(closeTimeParts[1])
  
  // 判斷當前時間是否在開館時間內
  isLibraryOpenNow.value = currentTime >= openTimeMinutes && currentTime < closeTimeMinutes
}

// 獲取統計數據
const fetchStats = async () => {
  loading.value = true
  try {
    const response = await axiosInstance.get('/stats')
    stats.value = response.data
  } catch (err) {
    error.value = '統計資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 切換頁籤
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'current') {
    if (viewMode.value === 'table') {
      fetchCurrentData()
    } else {
      fetchGroupedData()
    }
  } else if (tab === 'stats') {
    fetchStats()
  }
}

// 切換檢視模式
const switchViewMode = (mode) => {
  viewMode.value = mode
  if (mode === 'table') {
    fetchCurrentData()
  } else {
    fetchGroupedData()
  }
}

// 初始加載
onMounted(() => {
  fetchGroupedData() // 預設使用分組檢視
  fetchLibraryHours() // 獲取圖書館營業時間
  
  // 每分鐘更新一次開放狀態
  setInterval(checkLibraryOpenStatus, 60000)
  
  // 更新系統時間
  updateSystemTime()
  // 每秒更新一次系統時間
  setInterval(updateSystemTime, 1000)
})

// 分館列表
const branchNames = computed(() => {
  return Object.keys(groupedAreas.value).sort()
})

// 更新時間
const updateTime = computed(() => {
  // 獲取當前是否為週日或週一
  const now = new Date();
  const dayOfWeek = now.getDay();
  const isSundayOrMonday = dayOfWeek === 0 || dayOfWeek === 1;
  
  // 如果圖書館已閉館，顯示閉館時間
  if (!isLibraryOpenNow.value) {
    const closeTimeStr = isSundayOrMonday ? libraryHours.value.weekendCloseTime : libraryHours.value.weekdayCloseTime;
    // 添加日誌輸出
    console.log("圖書館已閉館，顯示閉館時間: " + closeTimeStr);
    return formatTime(closeTimeStr);
  }
  
  // 原有邏輯處理開館時間
  if (viewMode.value === 'table' && areas.value.length > 0) {
    return formatDateTime(areas.value[0].recordTime)
  } else if (viewMode.value === 'group' && Object.keys(groupedAreas.value).length > 0) {
    const firstBranch = Object.keys(groupedAreas.value)[0]
    if (groupedAreas.value[firstBranch] && groupedAreas.value[firstBranch].length > 0) {
      return formatDateTime(groupedAreas.value[firstBranch][0].recordTime)
    }
  }
  return ''
})

// 從後端獲取的原始時間
const rawServerTime = computed(() => {
  if (viewMode.value === 'table' && areas.value.length > 0) {
    return areas.value[0].recordTime
  } else if (viewMode.value === 'group' && Object.keys(groupedAreas.value).length > 0) {
    const firstBranch = Object.keys(groupedAreas.value)[0]
    if (groupedAreas.value[firstBranch] && groupedAreas.value[firstBranch].length > 0) {
      return groupedAreas.value[firstBranch][0].recordTime
    }
  }
  return ''
})

// 格式化日期時間
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  
  try {
    // 明確建立一個台北時間（UTC+8）
    const date = new Date(dateTimeStr);
    
    // 使用完整指定的台北時區格式
    const options = { 
      year: 'numeric', 
      month: '2-digit', 
      day: '2-digit',
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false,
      timeZone: 'Asia/Taipei'
    };
    
    // 輸出日期和時間以便調試
    console.log('原始日期時間:', dateTimeStr);
    console.log('格式化後時間:', date.toLocaleString('zh-TW', options));
    
    // 格式化為台北時間
    return date.toLocaleString('zh-TW', options).replace(/\//g, '-');
  } catch (error) {
    console.error('時間格式化錯誤:', error);
    // 備用方案 - 手動調整為UTC+8
    const date = new Date(dateTimeStr);
    // 輸出處理前後的時間以便調試
    console.log('原始時間:', date);
    console.log('UTC 時間:', date.getUTCHours());
    
    const utcHours = date.getUTCHours();
    const taipeiHours = (utcHours + 8) % 24; // UTC+8
    
    return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${taipeiHours.toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  
  // 使用toLocaleDateString並指定台北時區
  const options = { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    timeZone: 'Asia/Taipei'
  };
  
  try {
    const date = new Date(dateStr);
    return date.toLocaleDateString('zh-TW', options).replace(/\//g, '-');
  } catch (error) {
    console.error('日期格式化錯誤:', error);
    // 備用方案
    const date = new Date(dateStr);
    return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
  }
}

// 獲取熱門時段數據
const fetchPeakHoursData = async (areaId, date) => {
  if (!areaId || !date) return
  
  loading.value = true
  try {
    const response = await axiosInstance.get(`/history/area/${areaId}/date/${date}/busiest-hours`)
    peakHoursData.value = response.data.busiestHours
    peakHoursType.value = 'area'
  } catch (err) {
    error.value = '熱門時段資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 獲取分館熱門時段數據
const fetchBranchPeakHoursData = async (branchName, date) => {
  if (!branchName || !date) return
  
  loading.value = true
  try {
    const response = await axiosInstance.get(`/history/branch/${branchName}/date/${date}/busiest-hours`)
    peakHoursData.value = response.data.busiestHours
    peakHoursType.value = 'branch'
  } catch (err) {
    error.value = '分館熱門時段資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 選擇區域查看熱門時段
const selectAreaForPeakHours = (area) => {
  selectedArea.value = area
  selectedBranch.value = null
  fetchPeakHoursData(area.areaId, selectedDate.value)
  showPeakHours.value = true
}

// 選擇分館查看熱門時段
const selectBranchForPeakHours = (branchName) => {
  selectedBranch.value = branchName
  selectedArea.value = null
  fetchBranchPeakHoursData(branchName, selectedDate.value)
  showPeakHours.value = true
}

// 日期變更處理
const handleDateChange = (event) => {
  selectedDate.value = event.target.value
  if (selectedArea.value) {
    fetchPeakHoursData(selectedArea.value.areaId, selectedDate.value)
  } else if (selectedBranch.value) {
    fetchBranchPeakHoursData(selectedBranch.value, selectedDate.value)
  }
}

// 關閉熱門時段視窗
const closePeakHours = () => {
  showPeakHours.value = false
}

// 格式化時間為易讀格式（HH:MM）
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  return timeStr
}

// 格式化時間（HH:MM）
const formatTimeOnly = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  
  try {
    // 明確建立一個台北時間（UTC+8）
    const date = new Date(dateTimeStr);
    
    // 使用完整指定的台北時區格式
    const options = { 
      hour: '2-digit', 
      minute: '2-digit',
      hour12: false,
      timeZone: 'Asia/Taipei'
    };
    
    // 輸出原始和格式化時間以便調試
    console.log('時間格式化 - 原始:', dateTimeStr);
    console.log('時間格式化 - 結果:', date.toLocaleTimeString('zh-TW', options));
    
    return date.toLocaleTimeString('zh-TW', options);
  } catch (error) {
    console.error('時間格式化錯誤:', error);
    // 備用方案 - 手動調整為UTC+8
    const date = new Date(dateTimeStr);
    const utcHours = date.getUTCHours();
    const taipeiHours = (utcHours + 8) % 24; // UTC+8
    
    return `${taipeiHours.toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
}
</script>

<template>
  <div class="container py-4">
    <h1 class="text-center mb-4">臺北市圖書館座位查詢系統</h1>
    
    <!-- 圖書館開閉館時間提示 -->
    <div class="alert" :class="isLibraryOpenNow ? 'alert-success' : 'alert-warning'" role="alert">
      <div class="d-flex justify-content-between align-items-center">
        <div>
          <strong>圖書館狀態：</strong> {{ isLibraryOpenNow ? '開放中' : '已閉館' }}
          <span v-if="!isLibraryOpenNow" class="ms-2">(數據讀取已暫停)</span>
        </div>
        <div>
          <strong>營業時間：</strong> 
          週二至週六 {{ formatTime(libraryHours.weekdayOpenTime) }} - {{ formatTime(libraryHours.weekdayCloseTime) }} | 
          週日、週一 {{ formatTime(libraryHours.weekendOpenTime) }} - {{ formatTime(libraryHours.weekendCloseTime) }}
        </div>
      </div>
    </div>
    
    <!-- 頁籤選項和當前時間 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <ul class="nav nav-tabs mb-0">
        <li class="nav-item">
          <button 
            class="nav-link"
            :class="{ active: activeTab === 'current' }" 
            @click="switchTab('current')">
            當前座位可用性
          </button>
        </li>
        <li class="nav-item">
          <button 
            class="nav-link"
            :class="{ active: activeTab === 'stats' }" 
            @click="switchTab('stats')">
            統計資訊
          </button>
        </li>
      </ul>
      
      <!-- 當前系統時間 -->
      <div class="text-primary">
        <div class="fw-bold">{{ currentSystemTime }}</div>
        <div v-if="rawServerTime" class="small text-muted">
          後端資料時間: {{ updateTime }}
          <br>
          <small>原始: {{ rawServerTime }}</small>
        </div>
      </div>
    </div>
    
    <!-- 載入中提示 -->
    <div v-if="loading" class="d-flex justify-content-center my-5">
      <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">資料載入中...</span>
      </div>
      <span class="ms-3">資料載入中...</span>
    </div>
    
    <!-- 錯誤提示 -->
    <div v-if="error" class="alert alert-danger">
      {{ error }}
    </div>
    
    <!-- 當前座位可用性 -->
    <div v-if="activeTab === 'current' && !loading">
      <div class="d-flex justify-content-between align-items-center mb-3">
        <h2>當前座位可用情況 <span class="fs-6 text-muted">（當前時間：{{ currentSystemTime }}）</span></h2>
        
        <!-- 檢視模式切換 -->
        <div class="btn-group">
          <button 
            class="btn"
            :class="{ 'btn-primary': viewMode === 'group', 'btn-outline-primary': viewMode !== 'group' }" 
            @click="switchViewMode('group')">
            分館檢視
          </button>
          <button 
            class="btn"
            :class="{ 'btn-primary': viewMode === 'table', 'btn-outline-primary': viewMode !== 'table' }" 
            @click="switchViewMode('table')">
            表格檢視
          </button>
        </div>
      </div>
      
      <!-- 分館分組檢視 -->
      <div v-if="viewMode === 'group'" class="row g-4">
        <div v-for="branchName in branchNames" :key="branchName" class="col-12 col-xl-6">
          <div class="card shadow-sm h-100">
            <div class="card-header d-flex justify-content-between">
              <h3 class="card-title h5 mb-0">{{ branchName }}</h3>
              <button class="btn btn-sm btn-primary" @click="selectBranchForPeakHours(branchName)">
                查看分館熱門時段
              </button>
            </div>
            <div class="card-body p-0">
              <div class="table-responsive">
                <table class="table table-striped table-hover mb-0">
                  <thead class="table-light">
                    <tr>
                      <th scope="col" style="width: 15%">樓層</th>
                      <th scope="col" style="width: 25%">區域名稱</th>
                      <th scope="col" style="width: 15%">剩餘座位數</th>
                      <th scope="col" style="width: 15%">總座位數</th>
                      <th scope="col" style="width: 15%">佔用率</th>
                      <th scope="col" style="width: 15%">操作</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="area in groupedAreas[branchName]" :key="area.areaId">
                      <td>{{ area.floorName }}</td>
                      <td>{{ area.areaName }}</td>
                      <td>{{ area.freeCount }}</td>
                      <td>{{ area.totalCount }}</td>
                      <td>
                        <div class="d-flex align-items-center">
                          <div class="progress flex-grow-1 me-2" style="height: 8px;">
                            <div 
                              class="progress-bar" 
                              :class="{
                                'bg-success': area.occupationRate < 50,
                                'bg-warning': area.occupationRate >= 50 && area.occupationRate < 80,
                                'bg-danger': area.occupationRate >= 80
                              }"
                              role="progressbar" 
                              :style="{ width: area.occupationRate + '%' }"
                              :aria-valuenow="area.occupationRate" 
                              aria-valuemin="0" 
                              aria-valuemax="100">
                            </div>
                          </div>
                          {{ area.occupationRate.toFixed(2) }}%
                        </div>
                      </td>
                      <td>
                        <button class="btn btn-sm btn-primary" @click="selectAreaForPeakHours(area)">熱門時段</button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 表格檢視 -->
      <div v-if="viewMode === 'table'" class="card shadow-sm">
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover mb-0">
              <thead class="table-light">
                <tr>
                  <th scope="col" style="width: 8%">區域編號</th>
                  <th scope="col" style="width: 18%">分館名稱</th>
                  <th scope="col" style="width: 8%">樓層</th>
                  <th scope="col" style="width: 18%">區域名稱</th>
                  <th scope="col" style="width: 12%">剩餘座位數</th>
                  <th scope="col" style="width: 12%">總座位數</th>
                  <th scope="col" style="width: 12%">佔用率</th>
                  <th scope="col" style="width: 12%">操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="area in areas" :key="area.areaId">
                  <td>{{ area.areaId }}</td>
                  <td>{{ area.branchName }}</td>
                  <td>{{ area.floorName }}</td>
                  <td>{{ area.areaName }}</td>
                  <td>{{ area.freeCount }}</td>
                  <td>{{ area.totalCount }}</td>
                  <td>
                    <div class="d-flex align-items-center">
                      <div class="progress flex-grow-1 me-2" style="height: 8px;">
                        <div 
                          class="progress-bar" 
                          :class="{
                            'bg-success': area.occupationRate < 50,
                            'bg-warning': area.occupationRate >= 50 && area.occupationRate < 80,
                            'bg-danger': area.occupationRate >= 80
                          }"
                          role="progressbar" 
                          :style="{ width: area.occupationRate + '%' }"
                          :aria-valuenow="area.occupationRate" 
                          aria-valuemin="0" 
                          aria-valuemax="100">
                        </div>
                      </div>
                      {{ area.occupationRate.toFixed(2) }}%
                    </div>
                  </td>
                  <td>
                    <button class="btn btn-sm btn-primary" @click="selectAreaForPeakHours(area)">熱門時段</button>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 統計資訊 -->
    <div v-if="activeTab === 'stats' && !loading && stats">
      <div class="card shadow-sm mb-4">
        <div class="card-header">
          <h2 class="card-title h5 mb-0">每日最高佔用率時段</h2>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-striped table-hover mb-0">
              <thead class="table-light">
                <tr>
                  <th scope="col" style="width: 15%">日期</th>
                  <th scope="col" style="width: 15%">時間</th>
                  <th scope="col" style="width: 25%">分館名稱</th>
                  <th scope="col" style="width: 25%">區域名稱</th>
                  <th scope="col" style="width: 20%">佔用率</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(area, index) in stats.dailyMaxOccupations" :key="index">
                  <td>{{ formatDate(area.recordTime) }}</td>
                  <td>{{ formatTimeOnly(area.recordTime) }}</td>
                  <td>{{ area.branchName }}</td>
                  <td>{{ area.areaName }}</td>
                  <td>
                    <div class="d-flex align-items-center">
                      <div class="progress flex-grow-1 me-2" style="height: 8px;">
                        <div 
                          class="progress-bar" 
                          :class="{
                            'bg-success': area.occupationRate < 50,
                            'bg-warning': area.occupationRate >= 50 && area.occupationRate < 80,
                            'bg-danger': area.occupationRate >= 80
                          }"
                          role="progressbar" 
                          :style="{ width: area.occupationRate + '%' }"
                          :aria-valuenow="area.occupationRate" 
                          aria-valuemin="0" 
                          aria-valuemax="100">
                        </div>
                      </div>
                      {{ area.occupationRate.toFixed(2) }}%
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      <div class="card shadow-sm mb-4">
        <div class="card-header">
          <h2 class="card-title h5 mb-0">佔用率最高的日期: {{ formatDate(stats.highestOccupationDate) }}</h2>
        </div>
        <div class="card-body">
          <p class="alert alert-info">
            最高佔用率: {{ stats.highestOccupationRate.toFixed(2) }}% ({{ formatDateTime(stats.highestOccupationTime) }})
          </p>
          
          <div class="table-responsive">
            <table class="table table-striped table-hover">
              <thead class="table-light">
                <tr>
                  <th scope="col" style="width: 25%">分館名稱</th>
                  <th scope="col" style="width: 30%">區域名稱</th>
                  <th scope="col" style="width: 20%">佔用率</th>
                  <th scope="col" style="width: 25%">時間</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(area, index) in stats.highestOccupationDay.slice(0, 10)" :key="index">
                  <td>{{ area.branchName }}</td>
                  <td>{{ area.areaName }}</td>
                  <td>
                    <div class="d-flex align-items-center">
                      <div class="progress flex-grow-1 me-2" style="height: 8px;">
                        <div 
                          class="progress-bar" 
                          :class="{
                            'bg-success': area.occupationRate < 50,
                            'bg-warning': area.occupationRate >= 50 && area.occupationRate < 80,
                            'bg-danger': area.occupationRate >= 80
                          }"
                          role="progressbar" 
                          :style="{ width: area.occupationRate + '%' }"
                          :aria-valuenow="area.occupationRate" 
                          aria-valuemin="0" 
                          aria-valuemax="100">
                        </div>
                      </div>
                      {{ area.occupationRate.toFixed(2) }}%
                    </div>
                  </td>
                  <td>{{ formatTimeOnly(area.recordTime) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      
      <!-- 圖表組件 -->
      <div class="card shadow-sm mb-4">
        <div class="card-body">
          <ChartComponent v-if="stats" :statsData="stats" />
        </div>
      </div>
    </div>
    
    <!-- 熱門時段彈出視窗 -->
    <div v-if="showPeakHours" class="modal d-block" tabindex="-1" style="background-color: rgba(0,0,0,0.5);">
      <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content">
          <div class="modal-header">
            <h5 class="modal-title">熱門時段查詢</h5>
            <button type="button" class="btn-close" @click="closePeakHours" aria-label="Close"></button>
          </div>
          <div class="modal-body">
            <div class="mb-3">
              <label for="date-picker" class="form-label">選擇日期：</label>
              <input 
                type="date" 
                class="form-control"
                id="date-picker" 
                :value="selectedDate" 
                @change="handleDateChange" 
                :max="new Date().toISOString().split('T')[0]"
              />
            </div>
            
            <div v-if="selectedArea" class="card bg-light mb-3">
              <div class="card-body">
                <p class="mb-1"><strong>分館：</strong> {{ selectedArea.branchName }}</p>
                <p class="mb-1"><strong>樓層：</strong> {{ selectedArea.floorName }}</p>
                <p class="mb-0"><strong>區域：</strong> {{ selectedArea.areaName }}</p>
              </div>
            </div>
            
            <div v-else-if="selectedBranch" class="card bg-light mb-3">
              <div class="card-body">
                <p class="mb-0"><strong>分館：</strong> {{ selectedBranch }}</p>
              </div>
            </div>
            
            <div v-if="loading" class="text-center py-4">
              <div class="spinner-border text-primary" role="status">
                <span class="visually-hidden">載入熱門時段資料中...</span>
              </div>
              <p class="mt-2">載入熱門時段資料中...</p>
            </div>
            
            <div v-else-if="peakHoursData.length === 0" class="alert alert-warning">
              沒有該日期的資料
            </div>
            
            <PeakHoursComponent 
              v-else
              :hoursData="peakHoursData" 
              :selectedDate="selectedDate" 
              :selectedArea="selectedArea"
              :selectedBranch="selectedBranch"
              :type="peakHoursType"
            />
          </div>
          <div class="modal-footer">
            <button type="button" class="btn btn-secondary" @click="closePeakHours">關閉</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template> 
