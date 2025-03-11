<template>
  <div class="delete-route">
    <h2>删除路线</h2>
    <el-form :inline="true">
      <el-form-item label="出发城市">
        <el-autocomplete
          v-model="deleteForm.from"
          :fetch-suggestions="getCitySuggestions"
          placeholder="出发城市"
          clearable
          @change="handleFromCityChange"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="searchAdjacentCities">查询相邻站</el-button>
      </el-form-item>
    </el-form>

    <!-- 显示相邻城市列表 -->
    <div v-if="adjacentCities?.length > 0" class="adjacent-cities">
      <h3>相邻站点</h3>
      <el-radio-group v-model="deleteForm.to" @change="handleToStationSelect">
        <el-radio v-for="city in adjacentCities" :key="city" :label="city">
          {{ city }}
        </el-radio>
      </el-radio-group>
    </div>

    <!-- 显示可删除的路线 -->
    <div v-if="routes?.length > 0" class="route-table">
      <h3>可删除的路线</h3>
      <el-table :data="routes" style="width: 100%; margin-top: 20px;">
        <el-table-column prop="fromCity" label="出发城市" />
        <el-table-column prop="toCity" label="目的城市" />
        <el-table-column prop="type" label="交通类型" />
        <el-table-column prop="routeNo" label="航班号/车次" />
        <el-table-column prop="departure" label="出发时间" />
        <el-table-column prop="arrival" label="到达时间" />
        <el-table-column label="费用">
          <template #default="scope">
            {{ scope.row.formattedFare || '0.0 元' }}
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template #default="scope">
            <el-button type="danger" size="small" @click="handleDeleteRoute(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <div v-else-if="hasSearched" class="no-data">
      <el-empty description="未找到相关路线"></el-empty>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { getDirectRoutes, getCities, getAdjacentCities, deleteRoute, getRouteDeleteConfirm } from '@/api/traffic'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'DeleteRoute',
  
  setup() {
    const deleteForm = reactive({
      from: '',
      to: ''
    })
    
    const adjacentCities = ref([])
    const routes = ref([])
    const hasSearched = ref(false)

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
      if (!deleteForm.from) {
        ElMessage.warning('请选择出发城市')
        return
      }

      try {
        const res = await getAdjacentCities(deleteForm.from)
        if (res.code === 200 && res.data) {
          // 从 data.adjacentStations 获取相邻站点数组
          adjacentCities.value = res.data.adjacentStations || []
          deleteForm.to = ''
          routes.value = []
          
          if (adjacentCities.value.length === 0) {
            ElMessage.info('该城市没有相邻站点')
          } else {
            ElMessage.success(res.data.message || `找到 ${adjacentCities.value.length} 个相邻站点`)
          }
        } else {
          ElMessage.warning(res.message || '查询相邻站点失败')
          adjacentCities.value = []
        }
      } catch (error) {
        console.error('查询相邻站点失败:', error)
        ElMessage.error('查询失败：' + (error.response?.data?.message || '未知错误'))
        adjacentCities.value = []
      }
    }

    const handleToStationSelect = async (value) => {
      if (!value) return
      
      try {
        const res = await getDirectRoutes(deleteForm.from, value)
        hasSearched.value = true
        
        if (res.code === 200) {
          routes.value = res.data || []
          
          if (routes.value.length === 0) {
            ElMessage.info('未找到相关路线')
          }
        } else {
          ElMessage.warning(res.message || '查询失败')
          routes.value = []
        }
      } catch (error) {
        console.error('查询失败:', error)
        ElMessage.error('查询失败：' + (error.response?.data?.message || '未知错误'))
        routes.value = []
      }
    }

    const handleDeleteRoute = async (route) => {
      try {
        const confirmRes = await getRouteDeleteConfirm(
          route.fromCity,
          route.toCity,
          route.type,
          route.routeNo
        )

        if (confirmRes.code === 200) {
          await ElMessageBox.confirm(
            confirmRes.data?.routeDescription || '确定要删除该路线吗？',
            '删除确认',
            {
              confirmButtonText: '确定删除',
              cancelButtonText: '取消',
              type: 'warning'
            }
          )

          const data = {
            fromCity: route.fromCity,
            toCity: route.toCity,
            routeNo: route.routeNo,
            departure: route.departure
          }

          const res = await deleteRoute(data)

          if (res.code === 200) {
            ElMessage.success('删除成功')
            // 重新查询路线列表
            if (deleteForm.from && deleteForm.to) {
              await handleToStationSelect(deleteForm.to)
            }
          } else {
            ElMessage.error(res.message || '删除失败')
          }
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          ElMessage.error(error.response?.data?.message || '删除失败')
        }
      }
    }

    const handleFromCityChange = (value) => {
      deleteForm.to = ''
      adjacentCities.value = []
      routes.value = []
      hasSearched.value = false
    }

    return {
      deleteForm,
      adjacentCities,
      routes,
      hasSearched,
      getCitySuggestions,
      searchAdjacentCities,
      handleToStationSelect,
      handleDeleteRoute,
      handleFromCityChange
    }
  }
}
</script>

<style scoped>
.delete-route {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
}

h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 30px;
  text-align: center;
}

:deep(.el-form) {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

:deep(.el-form-item) {
  margin-bottom: 0;
}

:deep(.el-autocomplete) {
  width: 200px;
}

/* 相邻站点样式 */
.adjacent-cities {
  width: 100%;
  max-width: 800px;
  margin: 20px 0;
  text-align: center;
}

.adjacent-cities h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
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
  width: 100%;
  max-width: 1200px;
  margin-top: 30px;
}

.route-table h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 20px;
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