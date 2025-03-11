<template>
  <div class="direct-route">
    <div class="direct-route-wrapper">
      <div class="direct-route-form">
        <h2>查询两站间班次</h2>
        <el-form :inline="true">
          <el-form-item label="出发城市">
            <el-autocomplete
              v-model="queryForm.from"
              :fetch-suggestions="getCitySuggestions"
              placeholder="出发城市"
              clearable
              @change="handleFromCityChange"
              class="city-input"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="searchAdjacentCities">查询相邻站</el-button>
            <el-button 
              type="primary" 
              @click="handleSwapStations"
              :disabled="!canSwap"
              title="交换起点和终点"
              class="swap-button"
            >
              交换
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 显示相邻城市列表 -->
      <div v-if="stationInfo.adjacentStations?.length > 0" class="adjacent-cities">
        <h3>相邻站点</h3>
        <el-radio-group v-model="selectedStation" @change="handleToStationSelect">
          <el-radio v-for="station in stationInfo.adjacentStations" :key="station" :label="station">
            {{ station }}
          </el-radio>
        </el-radio-group>
      </div>

      <!-- 显示路线结果 -->
      <div v-if="stationInfo.directRoutes?.length > 0" class="route-table">
        <h3>查询结果</h3>
        <el-table :data="stationInfo.directRoutes">
          <el-table-column prop="fromCity" label="出发城市" align="center" min-width="120" />
          <el-table-column prop="toCity" label="目的城市" align="center" min-width="120" />
          <el-table-column prop="type" label="交通类型" align="center" width="100" />
          <el-table-column prop="routeNo" label="航班号/车次" align="center" min-width="130" />
          <el-table-column prop="departure" label="出发时间" align="center" min-width="100" />
          <el-table-column prop="arrival" label="到达时间" align="center" min-width="100" />
          <el-table-column label="费用" align="right" min-width="100">
            <template #default="scope">
              <span style="color: #f56c6c; font-weight: 500;">{{ scope.row.formattedFare || '0.0 元' }}</span>
            </template>
          </el-table-column>
        </el-table>
      </div>
      
      <div v-else-if="hasSearched" class="no-data">
        <el-empty description="未找到相关路线"></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { getDirectRoutes, getCities, getAdjacentCities } from '@/api/traffic'
import { ElMessage } from 'element-plus'

export default {
  name: 'DirectRoute',
  
  setup() {
    const queryForm = reactive({
      from: '',
      to: ''
    })
    
    const stationInfo = reactive({
      adjacentStations: [],
      directRoutes: []
    })
    
    const selectedStation = ref('')
    const hasSearched = ref(false)

    // 计算是否可以交换站点
    const canSwap = computed(() => {
      return selectedStation.value && queryForm.from
    })

    // 添加交换站点的方法
    const handleSwapStations = async () => {
      if (!canSwap.value) return

      const tempFrom = queryForm.from
      queryForm.from = selectedStation.value
      selectedStation.value = tempFrom

      // 清空当前路线结果
      stationInfo.directRoutes = []
      hasSearched.value = false

      // 查询新的相邻站点
      await searchAdjacentCities()
      
      // 如果原始起点在新的相邻站点列表中，自动选择并查询
      if (stationInfo.adjacentStations.includes(tempFrom)) {
        selectedStation.value = tempFrom
        await handleToStationSelect(tempFrom)
      }
    }

    const getCitySuggestions = async (query, cb) => {
      try {
        const res = await getCities(query)
        if (res.code === 200) {
          cb(res.data.map(city => ({ value: city })))
        } else {
          cb([])
        }
      } catch (error) {
        console.error('获取城市列表失败:', error)
        cb([])
      }
    }

    const searchAdjacentCities = async () => {
      if (!queryForm.from) {
        ElMessage.warning('请选择出发城市')
        return
      }

      try {
        const res = await getAdjacentCities(queryForm.from)
        if (res.code === 200 && res.data) {
          stationInfo.adjacentStations = res.data.adjacentStations || []
          selectedStation.value = ''
          stationInfo.directRoutes = []
          
          if (stationInfo.adjacentStations.length === 0) {
            ElMessage.info('该城市没有相邻站点')
          } else {
            ElMessage.success(res.data.message || `找到 ${stationInfo.adjacentStations.length} 个相邻站点`)
          }
        } else {
          ElMessage.warning(res.message || '查询相邻站点失败')
          stationInfo.adjacentStations = []
        }
      } catch (error) {
        console.error('查询相邻站点失败:', error)
        ElMessage.error('查询失败：' + (error.response?.data?.message || '未知错误'))
        stationInfo.adjacentStations = []
      }
    }

    const handleToStationSelect = async (value) => {
      if (!value) return
      
      try {
        const res = await getDirectRoutes(queryForm.from, value)
        hasSearched.value = true
        
        if (res.code === 200) {
          stationInfo.directRoutes = Array.isArray(res.data) ? res.data : []
          
          stationInfo.directRoutes = stationInfo.directRoutes.map(route => ({
            ...route,
            formattedFare: route.fare ? `¥${route.fare.toFixed(1)}` : '0.0 元'
          }))
          
          if (stationInfo.directRoutes.length === 0) {
            ElMessage.info('未找到直达路线')
          }
        } else {
          ElMessage.warning(res.message || '查询失败')
          stationInfo.directRoutes = []
        }
      } catch (error) {
        console.error('查询直达路线失败:', error)
        ElMessage.error('查询失败：' + (error.response?.data?.message || '未知错误'))
        stationInfo.directRoutes = []
      }
    }

    const handleFromCityChange = (value) => {
      queryForm.to = ''
      if (!value) {
        stationInfo.adjacentStations = []
        return
      }
    }

    return {
      queryForm,
      stationInfo,
      selectedStation,
      hasSearched,
      canSwap,
      getCitySuggestions,
      searchAdjacentCities,
      handleToStationSelect,
      handleFromCityChange,
      handleSwapStations
    }
  }
}
</script>

<style scoped>
.direct-route {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.direct-route-wrapper {
  width: 90%;
  max-width: 1200px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

.direct-route-form {
  margin-bottom: 30px;
  text-align: center;
}

.direct-route-form h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 20px;
}

.city-input {
  width: 200px;
}

:deep(.el-form) {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

.swap-button {
  margin-left: 10px;
  border-radius: 4px;
}

/* 相邻站点样式 */
.adjacent-cities {
  margin: 20px 0;
  text-align: center;
}

.adjacent-cities h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
}

:deep(.el-radio-group) {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 15px;
}

:deep(.el-radio) {
  margin-right: 0;
}

/* 路线表格样式 */
.route-table {
  margin-top: 30px;
}

.route-table h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 15px;
  text-align: center;
}

:deep(.el-table) {
  margin-top: 15px;
}

:deep(.el-table th) {
  background-color: #f5f7fa;
  color: #606266;
  font-weight: 500;
}

:deep(.el-table td) {
  padding: 12px 0;
}

.no-data {
  margin-top: 30px;
  text-align: center;
}
</style> 