<template>
  <div class="add-city-container">
    <div class="add-city-wrapper">
      <h2 class="add-city-title">添加新城市</h2>
      <el-form class="add-city-form">
        <el-form-item label="城市名称">
          <el-input v-model="cityForm.name" placeholder="请输入城市名称" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleAddCity">添加城市</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script>
import { reactive } from 'vue'
import { addCity } from '@/api/traffic'
import { ElMessage } from 'element-plus'

export default {
  name: 'AddCity',
  
  setup() {
    const cityForm = reactive({
      name: ''
    })

    const handleAddCity = async () => {
      if (!cityForm.name) {
        ElMessage.warning('请输入城市名称')
        return
      }

      try {
        const res = await addCity(cityForm.name.trim())
        if (res.code === 200) {
          ElMessage.success('添加城市成功')
          cityForm.name = ''
        } else {
          ElMessage.error(res.message || '添加失败')
        }
      } catch (error) {
        console.error('添加城市失败:', error)
        ElMessage.error('添加城市失败：' + (error.response?.data?.message || '未知错误'))
      }
    }

    return {
      cityForm,
      handleAddCity
    }
  }
}
</script>

<style>
.add-city-container {
  width: 100%;
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 20px;
  background-color: #f5f7fa;
}

.add-city-wrapper {
  width: 90%;
  max-height: 382px;
  max-width: 800px;
  margin-top: 60px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 40px;
}

.add-city-title {
  font-size: 32px;
  color: #303133;
  text-align: center;
  margin-bottom: 40px;
  font-weight: 500;
}

.add-city-form {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

.add-city-form .el-form-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.add-city-form .el-form-item__label {
  font-size: 18px;
  text-align: left;
  width: 80px;
  margin-bottom: 12px;
  line-height: 1.5;
  padding: 0;
}

.add-city-form .el-form-item__content {
  width: 300px;
  margin: 0 auto;
  display: flex;
  justify-content: center;
}

.add-city-form .el-input {
  width: 100%;
}

.add-city-form .el-input :deep(.el-input__inner) {
  text-align: center;
  font-size: 16px;
}

.add-city-form .el-button {
  width: 180px;
  height: 44px;
  font-size: 16px;
  display: block;
  margin: 40px auto 0;
}
</style> 