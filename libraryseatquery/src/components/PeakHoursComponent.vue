<script setup>
import { ref, onMounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const props = defineProps({
  hoursData: Array,
  selectedDate: String,
  selectedArea: Object,
  selectedBranch: String,
  type: {
    type: String,
    default: 'area'
  }
});

const chartCanvas = ref(null);
let hoursChart = null;

// 初始化圖表
const initChart = () => {
  if (!props.hoursData || props.hoursData.length === 0) return;
  
  // 固定時間軸（6時到21時）
  const fixedHours = Array.from({ length: 16 }, (_, i) => i + 6);
  
  // 準備數據 - 創建一個包含所有小時的數據陣列，沒有數據的小時設為null
  const ratesMap = new Map();
  props.hoursData.forEach(item => {
    const hour = parseInt(item[0]);
    const rate = item[1];
    ratesMap.set(hour, rate);
  });
  
  // 使用固定時間軸，沒有數據的地方設為null
  const rates = fixedHours.map(hour => ratesMap.has(hour) ? ratesMap.get(hour) : null);
  
  // 獲取畫布上下文
  const ctx = chartCanvas.value.getContext('2d');
  
  // 如果圖表已存在，先銷毀
  if (hoursChart) {
    hoursChart.destroy();
  }
  
  // 設定顏色，繁忙時段顏色較深
  const barColors = rates.map(rate => {
    if (rate === null) return 'rgba(0, 0, 0, 0)'; // 透明
    // 使用單一藍色，不再根據佔用率調整顏色深淺
    return 'rgba(66, 133, 244, 1)'; // Google藍色
  });
  
  // 創建圖表
  hoursChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: fixedHours.map(hour => `${hour}`),
      datasets: [
        {
          label: '平均佔用率 (%)',
          data: rates,
          backgroundColor: barColors,
          borderColor: 'transparent',
          borderWidth: 0,
          borderRadius: 2,
          barThickness: 10,
          maxBarThickness: 10
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: {
          display: false
        },
        title: {
          display: false
        },
        tooltip: {
          callbacks: {
            title: function(tooltipItems) {
              const hour = tooltipItems[0].label;
              return `${hour}時`;
            },
            label: function(context) {
              if (context.raw === null) return '';
              return `佔用率: ${context.raw.toFixed(1)}%`;
            }
          },
          // 只在有數據的位置顯示tooltip
          filter: function(tooltipItem) {
            return tooltipItem.raw !== null;
          },
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          padding: 10,
          titleFont: {
            size: 14,
            weight: 'bold'
          },
          bodyFont: {
            size: 13
          },
          displayColors: false
        }
      },
      scales: {
        y: {
          display: false,
          beginAtZero: true,
          max: 100
        },
        x: {
          grid: {
            display: false,
            drawBorder: false
          },
          ticks: {
            // 只顯示部分刻度
            callback: function(value, index) {
              const hour = this.getLabelForValue(index);
              // 只顯示6, 9, 12, 15, 18, 21
              return [6, 9, 12, 15, 18, 21].includes(parseInt(hour)) ? `${hour}時` : '';
            },
            color: '#6c757d',
            font: {
              size: 11
            },
            padding: 10
          }
        }
      },
      // 加上底線
      layout: {
        padding: {
          left: 0,
          right: 0,
          top: 20,
          bottom: 10
        }
      },
      // 禁止點擊沒有數據的柱子
      events: ['mousemove', 'mouseout', 'click', 'touchstart', 'touchmove'],
      onClick: function(e, elements) {
        if (elements.length > 0) {
          const index = elements[0].index;
          const value = this.data.datasets[0].data[index];
          if (value === null) {
            // 取消事件
            e.native.stopPropagation();
          }
        }
      }
    }
  });
};

// 監視數據變化，更新圖表
watch(() => props.hoursData, () => {
  initChart();
}, { deep: true });

onMounted(() => {
  if (props.hoursData && props.hoursData.length > 0) {
    initChart();
  }
});

// 根據類型獲取標題
const getTitle = () => {
  if (props.type === 'area' && props.selectedArea) {
    return props.selectedArea.areaName;
  } else if (props.type === 'branch' && props.selectedBranch) {
    return props.selectedBranch;
  }
  return '';
};
</script>

<template>
  <div class="mb-3">
    <div class="d-flex justify-content-between align-items-center mb-2">
      <h5 class="mb-0">
        熱門時段 
        <span v-if="type === 'area' && selectedArea" class="text-muted fs-6">{{ selectedArea.areaName }}</span>
        <span v-else-if="type === 'branch' && selectedBranch" class="text-muted fs-6">{{ selectedBranch }} (整館)</span>
      </h5>
      <div class="date-display" v-if="selectedDate">{{ selectedDate }}</div>
    </div>
    <div class="chart-container position-relative">
      <canvas ref="chartCanvas"></canvas>
      <div class="google-style-line"></div>
    </div>
  </div>
</template>

<style scoped>
.date-display {
  font-size: 14px;
  color: #6c757d;
}

.chart-container {
  position: relative;
  height: 170px;
  margin-bottom: 5px;
}

.google-style-line {
  position: absolute;
  bottom: 25px;
  left: 0;
  right: 0;
  height: 1px;
  background-color: #e0e0e0;
  z-index: 0;
}
</style> 