<script setup>
import { ref, onMounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';

Chart.register(...registerables);

const props = defineProps({
  hoursData: Array,
  selectedDate: String,
  selectedArea: Object
});

const chartCanvas = ref(null);
let hoursChart = null;

// 初始化圖表
const initChart = () => {
  if (!props.hoursData || props.hoursData.length === 0) return;
  
  // 準備數據
  const hours = props.hoursData.map(item => `${item[0]}時`);
  const rates = props.hoursData.map(item => item[1]);
  
  // 獲取畫布上下文
  const ctx = chartCanvas.value.getContext('2d');
  
  // 如果圖表已存在，先銷毀
  if (hoursChart) {
    hoursChart.destroy();
  }
  
  // 設定顏色，繁忙時段顏色較深
  const barColors = rates.map(rate => {
    if (rate < 30) {
      return 'rgba(40, 167, 69, 0.6)'; // 綠色 (低佔用)
    } else if (rate < 70) {
      return 'rgba(255, 193, 7, 0.6)'; // 黃色 (中等佔用)
    } else {
      return 'rgba(220, 53, 69, 0.6)'; // 紅色 (高佔用)
    }
  });
  
  // 創建圖表
  hoursChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: hours,
      datasets: [
        {
          label: '平均佔用率 (%)',
          data: rates,
          backgroundColor: barColors,
          borderColor: barColors.map(color => color.replace('0.6', '1')),
          borderWidth: 1
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
            label: function(context) {
              return `佔用率: ${context.raw.toFixed(1)}%`;
            }
          }
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
            display: false
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
</script>

<template>
  <div class="mb-3">
    <div class="d-flex justify-content-between align-items-center mb-2">
      <h5 class="mb-0">熱門時段 <span v-if="selectedArea" class="text-muted fs-6">{{ selectedArea.areaName }}</span></h5>
      <div class="date-display" v-if="selectedDate">{{ selectedDate }}</div>
    </div>
    <div class="chart-container position-relative" style="height: 150px;">
      <canvas ref="chartCanvas"></canvas>
    </div>
    <div class="d-flex justify-content-between mt-2 text-muted small">
      <div>6時</div>
      <div>9時</div>
      <div>12時</div>
      <div>15時</div>
      <div>18時</div>
      <div>21時</div>
    </div>
  </div>
</template>

<style scoped>
.date-display {
  font-size: 14px;
  color: #6c757d;
}
</style> 