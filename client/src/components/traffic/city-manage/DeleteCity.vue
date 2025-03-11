<template>
  <div class="delete-city-container">
    <div class="delete-city-wrapper">
      <h2 class="delete-city-title">删除城市</h2>
      <el-form class="delete-city-form">
        <el-form-item label="城市名称">
          <el-autocomplete
            v-model="deleteCityForm.name"
            :fetch-suggestions="getCitySuggestions"
            placeholder="请选择要删除的城市"
            clearable
            class="city-input"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="danger" @click="handleDeleteCity">删除城市</el-button>
        </el-form-item>
      </el-form>

      <!-- 如果有相关路线，显示路线列表 -->
      <div v-if="(deleteCityInfo?.relatedRoutes || []).length > 0" class="related-routes">
        <h3 class="related-routes-title">相关路线</h3>
        <el-table :data="deleteCityInfo?.relatedRoutes || []" class="routes-table">
          <el-table-column prop="fromCity" label="出发城市" />
          <el-table-column prop="toCity" label="目的城市" />
          <el-table-column prop="type" label="交通类型" />
          <el-table-column prop="routeNo" label="航班号/车次" />
          <el-table-column prop="departure" label="出发时间" />
          <el-table-column prop="arrival" label="到达时间" />
          <el-table-column prop="fare" label="费用" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { deleteCity, getCities, getCityDeleteInfo } from '@/api/traffic'
import { ElMessage, ElMessageBox } from 'element-plus'

export default {
  name: 'DeleteCity',
  
  setup() {
    const deleteCityForm = reactive({
      name: ''
    })

    const deleteCityInfo = ref({
      relatedRoutes: [],
      confirmMessage: ''
    })

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

    const handleDeleteCity = async () => {
      if (!deleteCityForm.name) {
        ElMessage.warning('请选择要删除的城市')
        return
      }

      try {
        const cityName = deleteCityForm.name.trim()

        // 直接显示确认对话框
        await ElMessageBox.confirm(
          `确定要删除城市"${cityName}"吗？此操作将同时删除所有相关路线。`,
          '删除确认',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'warning',
            confirmButtonClass: 'el-button--danger'
          }
        )

        // 执行删除操作
        const res = await deleteCity(cityName)

        if (res.code === 200) {
          ElMessage.success('删除城市成功')
          deleteCityForm.name = ''
          deleteCityInfo.value = { relatedRoutes: [], confirmMessage: '' }
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (error) {
        if (error === 'cancel') {
          return
        }
        
        console.error('删除城市失败:', error)
        const errorMessage = error.response?.data?.message || 
          error.message || 
          '删除失败，请稍后重试'
        
        ElMessage.error('删除失败：' + errorMessage)
      }
    }

    return {
      deleteCityForm,
      deleteCityInfo,
      getCitySuggestions,
      handleDeleteCity
    }
  }
}
</script>

<style>
.delete-city-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  background-color: #f5f7fa;
}

.delete-city-wrapper {
  width: 90%;
  max-height: 382px;
  max-width: 800px;
  margin-top: 60px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.delete-city-title {
  font-size: 32px;
  color: #303133;
  text-align: center;
  margin-bottom: 40px;
  font-weight: 500;
}

.delete-city-form {
  width: 100%;
  max-width: 500px;
  margin: 0 auto 40px;
}

.delete-city-form .el-form-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.delete-city-form .el-form-item__label {
  font-size: 18px;
  text-align: center;
  width: 80px;
  margin-bottom: 12px;
  line-height: 1.5;
  padding: 0;
}

.delete-city-form .el-form-item__content {
  width: 300px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.city-input {
  width: 100%;
}

.city-input :deep(.el-input__inner) {
  text-align: center;
  font-size: 16px;
}

.delete-city-form .el-button {
  width: 180px;
  height: 44px;
  font-size: 16px;
  display: block;
  margin: 40px auto 0;
}

.related-routes {
  margin-top: 40px;
  padding-top: 30px;
  border-top: 1px solid #ebeef5;
}

.related-routes-title {
  font-size: 24px;
  color: #303133;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 500;
}

.routes-table {
  width: 100%;
  margin-top: 20px;
}

.routes-table :deep(.el-table__header) {
  font-size: 16px;
}

.routes-table :deep(.el-table__body) {
  font-size: 14px;
}

.routes-table :deep(.el-table__cell) {
  text-align: center;
}

/* 确保下拉菜单宽度与输入框一致且内容居中 */
:deep(.el-autocomplete-suggestion) {
  width: 300px !important;
}

:deep(.el-autocomplete-suggestion__list) {
  text-align: center;
}
</style> 