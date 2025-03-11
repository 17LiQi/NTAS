<template>
  <div class="add-route">
    <h2>添加新路线</h2>
    <el-form label-width="120px">
      <el-form-item label="出发城市">
        <el-autocomplete 
          v-model="routeForm.fromCity" 
          :fetch-suggestions="getCitySuggestions" 
          placeholder="出发城市"
          clearable
        />
      </el-form-item>

      <el-form-item label="目的地城市">
        <el-autocomplete 
          v-model="routeForm.toCity" 
          :fetch-suggestions="getCitySuggestions" 
          placeholder="目的城市"
          clearable
        />
      </el-form-item>

      <el-form-item label="交通类型">
        <el-radio-group v-model="routeForm.type">
          <el-radio label="火车">火车</el-radio>
          <el-radio label="飞机">飞机</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item label="航班号/车次">
        <el-input v-model="routeForm.routeNo" placeholder="请输入航班号或车次" />
      </el-form-item>

      <el-form-item label="出发时间">
        <el-time-picker
          v-model="routeForm.departure"
          format="HH:mm"
          placeholder="选择出发时间"
          value-format="HH:mm"
          :arrow-control="true"
        />
      </el-form-item>

      <el-form-item label="到达时间">
        <el-time-picker
          v-model="routeForm.arrival"
          format="HH:mm"
          placeholder="选择到达时间"
          value-format="HH:mm"
          :arrow-control="true"
        />
      </el-form-item>

      <el-form-item label="费用">
        <el-input-number
          v-model="routeForm.fare"
          :min="0"
          :precision="1"
          :step="0.5"
          controls-position="right"
          placeholder="请输入费用"
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleAddRoute">添加路线</el-button>
      </el-form-item>
    </el-form>
  </div>
</template>

<script>
import { reactive } from 'vue'
import { addRoute, getCities } from '@/api/traffic'
import { ElMessage } from 'element-plus'

export default {
  name: 'AddRoute',
  
  setup() {
    const routeForm = reactive({
      fromCity: '',
      toCity: '',
      type: '火车',
      routeNo: '',
      departure: '',
      arrival: '',
      fare: 0
    })

    const getCitySuggestions = async (query, cb) => {
      try {
        const res = await getCities(query)
        if (res.code === 200) {
          // 如果是目的地城市，需要排除已选择的出发城市
          let cities = res.data
          if (routeForm.fromCity) {
            cities = cities.filter(city => city !== routeForm.fromCity)
          }
          cb(cities.map(city => ({ value: city })))
        } else {
          cb([])
        }
      } catch (error) {
        console.error('获取城市列表失败:', error)
        cb([])
      }
    }

    const handleAddRoute = async () => {
      try {
        const res = await addRoute(routeForm)
        if (res.code === 200) {
          ElMessage.success('添加成功')
          // 清空表单
          Object.assign(routeForm, {
            fromCity: '',
            toCity: '',
            type: '火车',
            routeNo: '',
            departure: '',
            arrival: '',
            fare: 0
          })
        }
      } catch (error) {
        ElMessage.error(error.response?.data?.message || '添加失败')
      }
    }

    return {
      routeForm,
      getCitySuggestions,
      handleAddRoute
    }
  }
}
</script>

<style scoped>
.add-route {
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
  width: 100%;
  max-width: 600px;
}

:deep(.el-form-item) {
  margin-bottom: 25px;
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #606266;
}

:deep(.el-input),
:deep(.el-autocomplete),
:deep(.el-time-picker),
:deep(.el-input-number) {
  width: 100%;
}

:deep(.el-radio-group) {
  display: flex;
  gap: 30px;
}

:deep(.el-button) {
  width: 160px;
  margin: 0 auto;
  display: block;
}

:deep(.el-form-item:last-child) {
  margin-top: 40px;
  margin-bottom: 0;
}

</style> 