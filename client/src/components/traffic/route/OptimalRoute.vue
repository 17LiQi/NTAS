<template>
  <div class="optimal-route-container">
    <div class="optimal-route-wrapper">
      <!-- 查询表单 -->
      <div class="optimal-route-form">
        <div class="route-inputs">
          <div class="city-input">
            <el-autocomplete
              v-model="optimalForm.start"
              :fetch-suggestions="getCitySuggestions"
              placeholder="请输入起点城市"
              clearable
              :trigger-on-focus="true"
              @change="handleStartCityChange"
            />
          </div>
          
          <el-button class="swap-button" @click="handleSwapCities">交换</el-button>
          
          <div class="city-input">
            <el-autocomplete
              v-model="optimalForm.end"
              :fetch-suggestions="getCitySuggestions"
              placeholder="请输入终点城市"
              clearable
              :trigger-on-focus="true"
              @change="handleEndCityChange"
            />
          </div>
        </div>

        <div class="optimization-criteria">
          <span class="criteria-title">优化标准：</span>
          <div class="criteria-group">
            <el-radio v-model="optimalForm.criterion" label="time">时间优先</el-radio>
            <el-radio v-model="optimalForm.criterion" label="cost">费用优先</el-radio>
          </div>
        </div>

        <el-button type="primary" class="search-button" @click="findOptimalRoute">
          查询最优路径
        </el-button>
      </div>

      <!-- 查询结果 -->
      <div v-if="routes.length > 0" class="optimal-result">
        <!-- 路线概览 -->
        <div class="route-overview">
          <div class="overview-item">
            <div class="label">完整路线</div>
            <div class="value">{{ routeInfo.path }}</div>
          </div>
          <div class="overview-item">
            <div class="label">总耗时</div>
            <div class="value">{{ routeInfo.totalDuration }}</div>
          </div>
          <div class="overview-item">
            <div class="label">总费用</div>
            <div class="value">{{ routeInfo.formattedFare }}</div>
          </div>
        </div>

        <!-- 详细行程 -->
        <div class="route-details">
          <h4>详细行程</h4>
          <div class="transfer-info">
            <div v-for="(route, index) in routes" :key="index" class="transfer-item">
              <span class="city">{{ route.fromCity }} → {{ route.toCity }}</span>
              <span class="route-info">
                {{ route.type === '火车' ? '火车' : '飞机' }} {{ route.routeNo }}
                {{ route.departure }} → {{ route.arrival }}
              </span>
              <span class="price">{{ route.formattedFare }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { getCities, getOptimalRoute } from '@/api/traffic'
import { ElMessage } from 'element-plus'

export default {
  name: 'OptimalRoute',
  setup() {
    const optimalForm = ref({
      start: '',
      end: '',
      criterion: 'time'
    })
    const routes = ref([])
    const routeInfo = ref({
      path: '',
      totalDuration: '',
      formattedFare: ''
    })

    const calculateTotalDuration = () => {
      if (!routes.value.length) return '0小时0分钟'
      
      let totalMinutes = 0
      routes.value.forEach(route => {
        const [depHour, depMin] = route.departure.split(':').map(Number)
        const [arrHour, arrMin] = route.arrival.split(':').map(Number)
        
        let minutes = (arrHour - depHour) * 60 + (arrMin - depMin)
        if (minutes < 0) {
          minutes += 24 * 60
        }
        totalMinutes += minutes
      })
      
      const hours = Math.floor(totalMinutes / 60)
      const mins = totalMinutes % 60
      return `${hours}小时${mins}分钟`
    }

    const calculateTotalFare = () => {
      if (!routes.value.length) return '¥0.0'
      const total = routes.value.reduce((sum, route) => {
        const fare = parseFloat(route.formattedFare.replace('¥', ''))
        return sum + fare
      }, 0)
      return `¥${total.toFixed(1)}`
    }

    const getCitySuggestions = async(query, cb) => {
      if (!query) {
        const response = await getCities()
        const cities = response.data.map(city => ({ value: city }))
        cb(cities)
        return
      }
      try {
        const response = await getCities(query)
        const cities = response.data.map(city => ({ value: city }))
        cb(cities)
      } catch (error) {
        console.error('获取城市建议失败:', error)
        cb([])
      }
    }

    const handleStartCityChange = (value) => {
      optimalForm.value.start = value
    }

    const handleEndCityChange = (value) => {
      optimalForm.value.end = value
    }

    const handleSwapCities = () => {
      const temp = optimalForm.value.start
      optimalForm.value.start = optimalForm.value.end
      optimalForm.value.end = temp
    }

    const findOptimalRoute = async () => {
      if (!optimalForm.value.start || !optimalForm.value.end) {
        ElMessage.warning('请输入起点和终点城市')
        return
      }

      try {
        const response = await getOptimalRoute(
          optimalForm.value.start,
          optimalForm.value.end,
          optimalForm.value.criterion
        )
        routes.value = response.data.routes
        
        // 构建完整路线
        const cities = routes.value.map((route, index) => {
          if (index === 0) {
            return `${route.fromCity} → ${route.toCity}`
          }
          return route.toCity
        })
        const path = cities.join(' → ')
        
        routeInfo.value = {
          path,
          totalDuration: response.data.totalTime || calculateTotalDuration(),
          formattedFare: `¥${response.data.totalFare.toFixed(1)}`
        }
      } catch (error) {
        console.error('查询最优路径失败:', error)
        ElMessage.error('查询最优路径失败')
      }
    }

    return {
      optimalForm,
      routes,
      routeInfo,
      getCitySuggestions,
      handleStartCityChange,
      handleEndCityChange,
      handleSwapCities,
      findOptimalRoute,
      calculateTotalDuration,
      calculateTotalFare
    }
  }
}
</script>

<style scoped>
.optimal-route-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.optimal-route-wrapper {
  width: 90%;
  max-width: 1200px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

/* 查询表单样式 */
.optimal-route-form {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.route-inputs {
  display: flex;
  align-items: center;
  gap: 20px;
  justify-content: center;
}

.city-input {
  width: 200px;
}

.swap-button {
  border-radius: 4px;
  margin: 0 10px;
}

.optimization-criteria {
  display: flex;
  align-items: center;
  gap: 20px;
}

.criteria-title {
  font-size: 16px;
  color: #606266;
}

.criteria-group {
  display: flex;
  gap: 30px;
}

.search-button {
  width: 160px;
  height: 40px;
}

/* 查询结果样式 */
.optimal-result {
  margin-top: 30px;
}

/* 路线概览样式 */
.route-overview {
  display: flex;
  flex-direction: column;
  gap: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 30px;
}

.overview-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.overview-item .label {
  font-size: 16px;
  color: #606266;
  width: 80px;
  text-align: right;
}

.overview-item .value {
  flex: 1;
  font-size: 16px;
  color: #303133;
  font-weight: 500;
  text-align: center;
}

/* 详细行程样式 */
.route-details {
  margin-top: 30px;
}

.route-details h4 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
  text-align: center;
}

.transfer-info {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.transfer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.transfer-item .city {
  font-size: 16px;
  color: #303133;
  font-weight: 500;
  width: 200px;
}

.transfer-item .route-info {
  flex: 1;
  text-align: center;
  color: #606266;
}

.transfer-item .price {
  font-size: 16px;
  color: #f56c6c;
  font-weight: 500;
  width: 100px;
  text-align: right;
}
</style> 