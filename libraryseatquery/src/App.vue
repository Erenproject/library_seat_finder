<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink, RouterView } from 'vue-router'
import ChartComponent from './components/ChartComponent.vue'

const areas = ref([])
const groupedAreas = ref({})
const stats = ref(null)
const error = ref('')
const loading = ref(false)
const activeTab = ref('current')
const viewMode = ref('group') // 'group' 或 'table' 檢視模式

// 獲取當前座位可用性（表格檢視）
const fetchCurrentData = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/library/current')
    if (!res.ok) throw new Error('API 載入失敗')
    areas.value = await res.json()
  } catch (err) {
    error.value = '資料載入失敗: ' + err.message
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 獲取按分館分組的座位資料
const fetchGroupedData = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/library/by-branch')
    if (!res.ok) throw new Error('API 載入失敗')
    groupedAreas.value = await res.json()
  } catch (err) {
    error.value = '資料載入失敗: ' + err.message
    console.error(err)
  } finally {
    loading.value = false
  }
}

// 獲取統計數據
const fetchStats = async () => {
  loading.value = true
  try {
    const res = await fetch('http://localhost:8080/api/library/stats')
    if (!res.ok) throw new Error('統計資料載入失敗')
    stats.value = await res.json()
  } catch (err) {
    error.value = '統計資料載入失敗: ' + err.message
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
</script>

<template>
  <div class="container">
    <h1>臺北市圖書館座位查詢系統</h1>
    
    <!-- 頁籤選項 -->
    <div class="tabs">
      <button 
        :class="{ active: activeTab === 'current' }" 
        @click="switchTab('current')">
        當前座位可用性
      </button>
      <button 
        :class="{ active: activeTab === 'stats' }" 
        @click="switchTab('stats')">
        統計資訊
      </button>
    </div>
    
    <!-- 載入中提示 -->
    <div v-if="loading" class="loading">
      資料載入中...
    </div>
    
    <!-- 錯誤提示 -->
    <div v-if="error" class="error">
      {{ error }}
    </div>
    
    <!-- 當前座位可用性 -->
    <div v-if="activeTab === 'current' && !loading">
      <h2>當前座位可用情況 <span v-if="updateTime">（更新時間：{{ updateTime }}）</span></h2>
      
      <!-- 檢視模式切換 -->
      <div class="view-mode">
        <button 
          :class="{ active: viewMode === 'group' }" 
          @click="switchViewMode('group')">
          分館檢視
        </button>
        <button 
          :class="{ active: viewMode === 'table' }" 
          @click="switchViewMode('table')">
          表格檢視
        </button>
      </div>
      
      <!-- 分館分組檢視 -->
      <div v-if="viewMode === 'group'" class="branch-groups">
        <div v-for="branchName in branchNames" :key="branchName" class="branch-card">
          <h3>{{ branchName }}</h3>
          <table>
            <thead>
              <tr>
                <th>樓層</th>
                <th>區域名稱</th>
                <th>剩餘座位數</th>
                <th>總座位數</th>
                <th>佔用率</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="area in groupedAreas[branchName]" :key="area.areaId">
                <td>{{ area.floorName }}</td>
                <td>{{ area.areaName }}</td>
                <td>{{ area.freeCount }}</td>
                <td>{{ area.totalCount }}</td>
                <td>{{ area.occupationRate.toFixed(2) }}%</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
      
      <!-- 表格檢視 -->
      <table v-if="viewMode === 'table'">
        <thead>
          <tr>
            <th>區域編號</th>
            <th>分館名稱</th>
            <th>樓層</th>
            <th>區域名稱</th>
            <th>剩餘座位數</th>
            <th>總座位數</th>
            <th>佔用率</th>
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
            <td>{{ area.occupationRate.toFixed(2) }}%</td>
          </tr>
        </tbody>
      </table>
    </div>
    
    <!-- 統計資訊 -->
    <div v-if="activeTab === 'stats' && !loading && stats">
      <div class="stats-section">
        <h2>每日最高佔用率時段</h2>
        <table>
          <thead>
            <tr>
              <th>日期</th>
              <th>時間</th>
              <th>分館名稱</th>
              <th>區域名稱</th>
              <th>佔用率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(area, index) in stats.dailyMaxOccupations" :key="index">
              <td>{{ formatDate(area.recordTime) }}</td>
              <td>{{ new Date(area.recordTime).toLocaleTimeString() }}</td>
              <td>{{ area.branchName }}</td>
              <td>{{ area.areaName }}</td>
              <td>{{ area.occupationRate.toFixed(2) }}%</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <div class="stats-section">
        <h2>佔用率最高的日期: {{ formatDate(stats.highestOccupationDate) }}</h2>
        <p>最高佔用率: {{ stats.highestOccupationRate.toFixed(2) }}% ({{ formatDateTime(stats.highestOccupationTime) }})</p>
        
        <table>
          <thead>
            <tr>
              <th>分館名稱</th>
              <th>區域名稱</th>
              <th>佔用率</th>
              <th>時間</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(area, index) in stats.highestOccupationDay.slice(0, 10)" :key="index">
              <td>{{ area.branchName }}</td>
              <td>{{ area.areaName }}</td>
              <td>{{ area.occupationRate.toFixed(2) }}%</td>
              <td>{{ new Date(area.recordTime).toLocaleTimeString() }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      
      <!-- 圖表組件 -->
      <ChartComponent v-if="stats" :statsData="stats" />
    </div>
  </div>
</template>

<style scoped>
.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  font-family: Arial, sans-serif;
}

h1 {
  text-align: center;
  color: #2c3e50;
  margin-bottom: 30px;
}

h2 {
  color: #3498db;
  margin-top: 20px;
  margin-bottom: 15px;
}

h3 {
  color: #2c3e50;
  margin-bottom: 10px;
  padding-bottom: 5px;
  border-bottom: 1px solid #eee;
}

.tabs, .view-mode {
  display: flex;
  margin-bottom: 20px;
  border-bottom: 1px solid #ddd;
}

.tabs button, .view-mode button {
  padding: 10px 20px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  color: #666;
}

.tabs button.active, .view-mode button.active {
  border-bottom: 3px solid #3498db;
  color: #3498db;
  font-weight: bold;
}

.view-mode {
  justify-content: flex-start;
  border-bottom: none;
  margin-top: -10px;
  margin-bottom: 20px;
}

.view-mode button {
  background: #f8f9fa;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-right: 10px;
  padding: 6px 12px;
}

.view-mode button.active {
  background: #3498db;
  color: white;
  border-color: #3498db;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 30px;
}

th, td {
  border: 1px solid #ddd;
  padding: 12px;
  text-align: center;
}

th {
  background-color: #f8f9fa;
  font-weight: bold;
}

tr:nth-child(even) {
  background-color: #f2f2f2;
}

.loading {
  text-align: center;
  padding: 30px;
  font-size: 18px;
  color: #666;
}

.error {
  background-color: #f8d7da;
  color: #721c24;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.stats-section {
  margin-bottom: 40px;
  border: 1px solid #eee;
  border-radius: 5px;
  padding: 20px;
  box-shadow: 0 2px 5px rgba(0,0,0,0.05);
}

.branch-groups {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
}

.branch-card {
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.05);
  transition: transform 0.2s;
}

.branch-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.08);
}

.branch-card table {
  margin-bottom: 0;
}

@media (max-width: 768px) {
  .branch-groups {
    grid-template-columns: 1fr;
  }
  
  .tabs button, .view-mode button {
    padding: 8px 12px;
    font-size: 14px;
  }
  
  th, td {
    padding: 8px;
    font-size: 14px;
  }
}
</style>
