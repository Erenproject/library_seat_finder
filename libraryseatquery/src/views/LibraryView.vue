<script setup>
import { ref, onMounted, computed } from 'vue'
import ChartComponent from '../components/ChartComponent.vue'
import PeakHoursComponent from '../components/PeakHoursComponent.vue'
import axios from 'axios'

// 設定 axios 基礎 URL
const apiBaseUrl = 'http://localhost:8080/api/library'
const axiosInstance = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000
})

const areas = ref([])
const groupedAreas = ref({})
const stats = ref(null)
const error = ref('')
const loading = ref(false)
const activeTab = ref('current')
const viewMode = ref('group') // 'group' 或 'table' 檢視模式

// 熱門時段相關
const peakHoursData = ref([])
const selectedArea = ref(null)
const selectedDate = ref(new Date().toISOString().split('T')[0])
const showPeakHours = ref(false)

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
  } catch (err) {
    error.value = '資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
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
})

// 分館列表
const branchNames = computed(() => {
  return Object.keys(groupedAreas.value).sort()
})

// 更新時間
const updateTime = computed(() => {
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

// 格式化日期時間
const formatDateTime = (dateTimeStr) => {
  if (!dateTimeStr) return '';
  const date = new Date(dateTimeStr);
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
}

// 獲取熱門時段數據
const fetchPeakHoursData = async (areaId, date) => {
  if (!areaId || !date) return
  
  loading.value = true
  try {
    const response = await axiosInstance.get(`/history/area/${areaId}/date/${date}/busiest-hours`)
    peakHoursData.value = response.data.busiestHours
  } catch (err) {
    error.value = '熱門時段資料載入失敗: ' + (err.response?.data?.message || err.message)
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 選擇區域查看熱門時段
const selectAreaForPeakHours = (area) => {
  selectedArea.value = area
  fetchPeakHoursData(area.areaId, selectedDate.value)
  showPeakHours.value = true
}

// 日期變更處理
const handleDateChange = (event) => {
  selectedDate.value = event.target.value
  if (selectedArea.value) {
    fetchPeakHoursData(selectedArea.value.areaId, selectedDate.value)
  }
}

// 關閉熱門時段視窗
const closePeakHours = () => {
  showPeakHours.value = false
}
</script>

<template>
  <div class="container py-4">
    <h1 class="text-center mb-4">臺北市圖書館座位查詢系統</h1>
    
    <!-- 頁籤選項 -->
    <ul class="nav nav-tabs mb-4">
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
        <h2>當前座位可用情況 <span v-if="updateTime" class="fs-6 text-muted">（更新時間：{{ updateTime }}）</span></h2>
        
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
            <div class="card-header">
              <h3 class="card-title h5 mb-0">{{ branchName }}</h3>
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
                  <td>{{ new Date(area.recordTime).toLocaleTimeString() }}</td>
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
                  <td>{{ new Date(area.recordTime).toLocaleTimeString() }}</td>
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