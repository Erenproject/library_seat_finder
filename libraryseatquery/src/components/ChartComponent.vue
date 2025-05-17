<script setup>
import { ref, onMounted, watch } from 'vue';
import { Chart, registerables } from 'chart.js';
import { defineProps } from 'vue';

Chart.register(...registerables);

const props = defineProps({
  statsData: Object
});

const chartCanvas = ref(null);
let occupationChart = null;

// 初始化圖表
const initChart = () => {
  if (!props.statsData || !props.statsData.dailyMaxOccupations) return;
  
  const dailyData = props.statsData.dailyMaxOccupations;
  
  // 準備數據
  const dates = dailyData.map(item => {
    const date = new Date(item.recordTime);
    return `${date.getFullYear()}-${(date.getMonth()+1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')}`;
  });
  
  const rates = dailyData.map(item => item.occupationRate);
  const libraries = dailyData.map(item => item.branchName);
  
  // 創建圖表
  const ctx = chartCanvas.value.getContext('2d');
  
  // 如果圖表已存在，先銷毀
  if (occupationChart) {
    occupationChart.destroy();
  }
  
  occupationChart = new Chart(ctx, {
    type: 'bar',
    data: {
      labels: dates,
      datasets: [
        {
          label: '每日最高佔用率 (%)',
          data: rates,
          backgroundColor: 'rgba(54, 162, 235, 0.5)',
          borderColor: 'rgba(54, 162, 235, 1)',
          borderWidth: 1
        }
      ]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'top',
        },
        title: {
          display: true,
          text: '每日最高佔用率圖表'
        },
        tooltip: {
          callbacks: {
            afterLabel: function(context) {
              return `分館: ${libraries[context.dataIndex]}`;
            }
          }
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          max: 100,
          title: {
            display: true,
            text: '佔用率 (%)'
          }
        },
        x: {
          title: {
            display: true,
            text: '日期'
          }
        }
      }
    }
  });
};

// 監視數據變化，更新圖表
watch(() => props.statsData, () => {
  initChart();
}, { deep: true });

onMounted(() => {
  if (props.statsData) {
    initChart();
  }
});
</script>

<template>
  <div class="chart-container">
    <h2>佔用率趨勢圖</h2>
    <canvas ref="chartCanvas" width="400" height="200"></canvas>
  </div>
</template>

<style scoped>
.chart-container {
  margin-top: 30px;
  margin-bottom: 40px;
  padding: 20px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #3498db;
}
</style> 