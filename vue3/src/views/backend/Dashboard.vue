<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <a-card class="welcome-card">
      <template #title>
        <div class="welcome-header">
          <a-avatar
            :size="64"
            :src="avatarUrl"
          >
            {{ userInfo?.name?.charAt(0) }}
          </a-avatar>
          <div class="welcome-info">
            <h2>欢迎回来，{{ userInfo?.name || userInfo?.username }}</h2>
            <p>{{ currentTime }}</p>
          </div>
        </div>
      </template>
      <div class="role-info">
        <a-tag>{{ roleLabel }}</a-tag>
      </div>
    </a-card>

    <AdminPageError :message="loadError" :loading="loading" @retry="fetchStatistics" />

    <!-- 数据统计卡片 -->
    <a-row
      :gutter="16"
      class="stats-row"
    >
      <a-col
        :xs="24"
        :sm="12"
        :lg="6"
      >
        <a-card
          class="stat-card"
          :loading="loading"
        >
          <a-statistic
            title="普通用户数"
            :value="statistics.totalUsers"
            :value-style="{ color: '#3f8600' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">
            今日新增: {{ statistics.todayNewUsers }}
          </div>
        </a-card>
      </a-col>
      <a-col
        :xs="24"
        :sm="12"
        :lg="6"
      >
        <a-card
          class="stat-card"
          :loading="loading"
        >
          <a-statistic
            title="总发帖数"
            :value="statistics.totalPosts"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <MessageOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">
            今日新增: {{ statistics.todayNewPosts }}
          </div>
        </a-card>
      </a-col>
      <a-col
        :xs="24"
        :sm="12"
        :lg="6"
      >
        <a-card
          class="stat-card"
          :loading="loading"
        >
          <a-statistic
            title="总管理员数"
            :value="statistics.totalAdmins"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <SafetyCertificateOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">
            -
          </div>
        </a-card>
      </a-col>
      <a-col
        :xs="24"
        :sm="12"
        :lg="6"
      >
        <a-card
          class="stat-card"
          :loading="loading"
        >
          <a-statistic
            title="网站总访问数"
            :value="statistics.totalVisits"
            :value-style="{ color: '#cf1322' }"
          >
            <template #prefix>
              <EyeOutlined />
            </template>
          </a-statistic>
          <div class="stat-sub">
            今日访问: {{ statistics.todayVisits }}
          </div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row
      :gutter="16"
      class="charts-row"
    >
      <!-- 访问趋势图 -->
      <a-col
        :xs="24"
        :lg="24"
      >
        <a-card
          title="近7天访问趋势"
          :loading="loading"
        >
          <div
            ref="visitChartRef"
            style="width: 100%; height: 300px;"
          />
        </a-card>
      </a-col>
    </a-row>

  </div>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useUserStore } from '@/store/user'
import AdminPageError from '@/components/backend/AdminPageError.vue'
import {
  UserOutlined,
  MessageOutlined,
  SafetyCertificateOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getDashboardStatistics } from '@/api/dashboard'

echarts.use([LineChart, GridComponent, TooltipComponent, CanvasRenderer])

const userStore = useUserStore()
const userInfo = computed(() => userStore.userInfo)

// 角色标签
const roleLabel = computed(() => {
  const roleMap = {
    'ADMIN': '系统管理员',
    'USER': '普通用户'
  }
  return roleMap[userInfo.value?.userType] || '未知角色'
})

const avatarUrl = computed(() => {
  return userInfo.value?.avatar;
})

// 当前时间
const currentTime = ref('')
let timeInterval = null // 保存定时器引用

const updateTime = () => {
  const now = new Date()
  const options = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
    hour: '2-digit',
    minute: '2-digit'
  }
  currentTime.value = now.toLocaleDateString('zh-CN', options)
}

// 统计数据
const loading = ref(false)
const loadError = ref('')
const statistics = ref({
  totalUsers: 0,
  todayNewUsers: 0,
  totalAdmins: 0,
  totalPosts: 0,
  todayNewPosts: 0,
  totalVisits: 0,
  todayVisits: 0,
  last7DaysVisits: []
})

// 图表引用
const visitChartRef = ref(null)
// ECharts实例
let visitChart = null

// 获取统计数据
const fetchStatistics = async () => {
  loading.value = true
  loadError.value = ''
  let loaded = false
  try {
    const data = await getDashboardStatistics({ showDefaultMsg: false })
    statistics.value = { ...statistics.value, ...(data || {}) }
    loaded = true
  } catch (error) {
    console.error('获取统计数据失败:', error)
    loadError.value = error?.message || '获取统计数据失败'
  } finally {
    loading.value = false
  }

  if (!loaded) return
  await nextTick()
  try {
    initCharts()
  } catch (error) {
    console.error('统计图表初始化失败:', error)
    loadError.value = error?.message || '统计图表加载失败'
  }
}

// 初始化所有图表
const initCharts = () => {
  initVisitChart()
}

// 初始化访问趋势图
const initVisitChart = () => {
  if (!visitChartRef.value) return

  if (visitChart) {
    visitChart.dispose()
  }

  visitChart = echarts.init(visitChartRef.value)

  const visits = statistics.value.last7DaysVisits || []
  const dates = visits.map(item => item.date)
  const counts = visits.map(item => item.count)

  const option = {
    tooltip: {
      trigger: 'axis'
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [{
      name: '访问量',
      type: 'line',
      data: counts,
      smooth: true,
      itemStyle: {
        color: '#1f5a50'
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [{
            offset: 0,
            color: 'rgba(31, 90, 80, 0.24)'
          }, {
            offset: 1,
            color: 'rgba(31, 90, 80, 0.03)'
          }]
        }
      }
    }]
  }

  visitChart.setOption(option)
}

// 窗口大小改变时重新调整图表大小
const handleResize = () => {
  visitChart?.resize()
}

onMounted(() => {
  updateTime()
  // 每分钟更新一次时间
  timeInterval = setInterval(updateTime, 60000)

  // 获取统计数据
  fetchStatistics()

  // 监听窗口大小变化
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  // 清除定时器
  if (timeInterval) {
    clearInterval(timeInterval)
    timeInterval = null
  }

  // 销毁图表实例
  visitChart?.dispose()

  // 移除窗口监听
  window.removeEventListener('resize', handleResize)
})
</script>

<style lang="scss" scoped>
.dashboard {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 64px);
}

.welcome-card {
  margin-bottom: 20px;

  .welcome-header {
    display: flex;
    align-items: center;
    gap: 20px;

    .welcome-info {
      flex: 1;

      h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
        color: #262626;
      }

      p {
        margin: 8px 0 0 0;
        font-size: 14px;
        color: #8c8c8c;
      }
    }
  }

  .role-info {
    margin-top: 16px;
  }
}

.stats-row {
  margin-bottom: 20px;

  .stat-card {
    text-align: center;

    .stat-sub {
      margin-top: 12px;
      font-size: 13px;
      color: #8c8c8c;
    }
  }
}

.charts-row {
  margin-bottom: 20px;
}

@media (min-width: 1024px) {
  .dashboard {
    min-height: calc(100vh - 112px);
    padding: 0;
    background: transparent;
  }

  .welcome-card {
    margin-bottom: 18px;
    border: 1px solid #dde4e1;
    border-radius: 8px;
    box-shadow: none;

    :deep(.ant-card-head) {
      min-height: 84px;
      padding: 0 22px;
      border-bottom: 0;
    }

    :deep(.ant-card-head-title) {
      padding: 14px 0 4px;
    }

    :deep(.ant-card-body) {
      padding: 0 22px 16px;
    }

    .welcome-header {
      gap: 14px;

      :deep(.ant-avatar) {
        width: 48px !important;
        height: 48px !important;
        line-height: 48px !important;
        background: #1f5a50;
      }

      .welcome-info h2 {
        color: #17211e;
        font-size: 22px;
        font-weight: 680;
      }

      .welcome-info p {
        margin-top: 3px;
        color: #66736f;
      }
    }

    .role-info {
      margin-top: 0;
    }
  }

  .stats-row {
    margin-bottom: 18px;

    .stat-card {
      min-height: 142px;
      text-align: left;
      border: 1px solid #dde4e1;
      border-radius: 8px;
      box-shadow: none;

      :deep(.ant-statistic-title) {
        color: #66736f;
        font-size: 13px;
      }

      :deep(.ant-statistic-content),
      :deep(.ant-statistic-content-value),
      :deep(.ant-statistic-content-prefix) {
        color: #1f5a50 !important;
      }

      .stat-sub {
        color: #66736f;
      }
    }
  }

  .charts-row :deep(.ant-card) {
    border: 1px solid #dde4e1;
    border-radius: 8px;
    box-shadow: none;
  }
}

@media (max-width: 768px) {
  .dashboard {
    padding: 12px;
  }

  .welcome-header {
    flex-direction: column;
    align-items: flex-start !important;

    .welcome-info h2 {
      font-size: 20px;
    }
  }

  .stats-row {
    .a-col {
      margin-bottom: 12px;
    }
  }
}
</style>
