<template>
  <div class="route-list-container">
    <div class="route-list-wrapper">
      <!-- 导航栏 -->
      <nav class="route-nav">
        <ul class="nav-list">
          <li class="nav-item active">所有路线</li>
        </ul>
      </nav>

      <!-- 路线列表 -->
      <div class="route-list">
        <template v-if="loading">
          <div class="loading-container">
            <el-skeleton :rows="3" animated />
          </div>
        </template>
        <template v-else-if="routes.length > 0">
          <div v-for="route in routes" :key="route.routeNo" class="route-item">
            <div class="route-info">
              <!-- 交通工具类型 -->
              <div class="transport-type">
                <div class="transport-icon">
                  <el-icon v-if="route.type === '火车'" class="train-icon">
                    <Van />
                  </el-icon>
                  <el-icon v-else class="plane-icon">
                    <Position />
                  </el-icon>
                </div>
                <div class="route-no">{{ route.routeNo }}</div>
              </div>

              <!-- 时间信息 -->
              <div class="time-info">
                <div class="departure">
                  <div class="time">{{ route.departure }}</div>
                  <div class="city">{{ route.fromCity }}</div>
                </div>

                <div class="duration">
                  <span>{{ calculateDuration(route.departure, route.arrival) }}</span>
                </div>

                <div class="arrival">
                  <div class="time">{{ route.arrival }}</div>
                  <div class="city">{{ route.toCity }}</div>
                </div>
              </div>
            </div>

            <!-- 价格信息 -->
            <div class="price-info">
              <div class="price">{{ route.formattedFare }}</div>
            </div>
          </div>
        </template>

        <!-- 无数据时显示 -->
        <el-empty v-else description="暂无路线信息"></el-empty>
      </div>
    </div>
  </div>
</template>

<script>
import { Van, Position } from '@element-plus/icons-vue'
import { getAllRoutes } from '@/api/traffic'

export default {
  name: 'RouteList',
  components: {
    Van,
    Position
  },
  data() {
    return {
      routes: [],
      loading: false
    }
  },
  created() {
    this.fetchRoutes()
  },
  methods: {
    calculateDuration(departure, arrival) {
      const [depHour, depMin] = departure.split(':').map(Number)
      const [arrHour, arrMin] = arrival.split(':').map(Number)
      
      let minutes = (arrHour - depHour) * 60 + (arrMin - depMin)
      if (minutes < 0) {
        minutes += 24 * 60
      }
      
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return `${hours}小时${mins}分钟`
    },
    async fetchRoutes() {
      this.loading = true
      try {
        const response = await getAllRoutes()
        this.routes = response.data
      } catch (error) {
        console.error('获取路线列表失败:', error)
        this.$message.error('获取路线列表失败')
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.route-list-container {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 20px;
}

.route-list-wrapper {
  width: 90%;
  max-width: 1200px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 20px;
}

/* 导航栏样式 */
.route-nav {
  border-bottom: 2px solid #e4e7ed;
  padding: 0 20px;
  margin-bottom: 30px;
  display: flex;
  justify-content: center;
}

.nav-list {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
  justify-content: center;
}

.nav-item {
  padding: 20px 40px;
  font-size: 18px;
  cursor: pointer;
  position: relative;
  color: #606266;
  transition: all 0.3s;
  height: 65px;
  line-height: 25px;
}

.nav-item.active {
  color: #409EFF;
  font-weight: 500;
  font-size: 20px;
}

.nav-item.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #409EFF;
}

/* 路线列表样式 */
.route-list {
  padding: 20px 40px;
}

.loading-container {
  padding: 20px;
}

.route-item {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 15px;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  transition: all 0.3s;
  background-color: #fff;
}

.route-item:hover {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

/* 路线信息样式 */
.route-info {
  display: flex;
  align-items: center;
  flex: 1;
  justify-content: space-between;
  padding: 0 40px;
}

.transport-type {
  width: 120px;
  text-align: center;
}

.transport-icon {
  font-size: 24px;
  margin-bottom: 5px;
}

:deep(.train-icon),
:deep(.plane-icon) {
  font-size: 28px;
  color: #409EFF;
}

.route-no {
  color: #606266;
  font-size: 14px;
}

.time-info {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 40px;
  flex: 1;
}

.departure,
.arrival {
  text-align: center;
}

.time {
  font-size: 24px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 8px;
}

.city {
  font-size: 16px;
  color: #606266;
}

.duration {
  text-align: center;
  color: #909399;
  font-size: 14px;
  position: relative;
  padding: 0 20px;
}

.duration::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  right: 0;
  height: 1px;
  background: #dcdfe6;
  z-index: 1;
}

.duration span {
  position: relative;
  background: #fff;
  padding: 0 10px;
  z-index: 2;
}

/* 价格信息样式 */
.price-info {
  min-width: 120px;
  text-align: right;
}

.price {
  font-size: 24px;
  color: #f56c6c;
  font-weight: 500;
}
</style> 