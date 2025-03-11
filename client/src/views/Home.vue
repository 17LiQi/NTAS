<template>
  <div class="home">
    <h1>交通分析系统</h1>
    <div class="content">
      <p>欢迎使用交通分析系统</p>
      <el-button type="primary" @click="goToTraffic">进入系统</el-button>
    </div>
  </div>
</template>

<script>
import { useRouter } from 'vue-router'

export default {
  name: 'Home',
  setup() {
    const router = useRouter()
    
    const goToTraffic = () => {
      router.push('/traffic')
    }

    return {
      goToTraffic
    }
  },
  mounted() {
    console.log('Home 组件已加载')
  }
}
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  text-align: center;
  background-color: #f5f7fa;
}

.content {
  margin-top: 20px;
}

h1 {
  color: #409EFF;
  margin-bottom: 20px;
}

p {
  color: #606266;
  margin-bottom: 30px;
}
</style> 