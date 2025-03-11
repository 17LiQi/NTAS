<template>
  <div class="traffic-manage">
    <el-tabs v-model="activeTab">
      <!-- 路线查询相关 -->
      <el-tab-pane label="路线列表" name="routeList">
        <route-list />
      </el-tab-pane>
      <el-tab-pane label="最优路径" name="optimalRoute">
        <optimal-route />
      </el-tab-pane>
      <el-tab-pane label="直达路线" name="directRoute">
        <direct-route />
      </el-tab-pane>

      <!-- 路线管理相关 -->
      <el-tab-pane label="添加路线" name="addRoute">
        <add-route />
      </el-tab-pane>
      <el-tab-pane label="删除路线" name="deleteRoute">
        <delete-route />
      </el-tab-pane>

      <!-- 城市管理相关 -->
      <el-tab-pane label="添加城市" name="addCity">
        <add-city />
      </el-tab-pane>
      <el-tab-pane label="删除城市" name="deleteCity">
        <delete-city />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import RouteList from './route/RouteList.vue'
import OptimalRoute from './route/OptimalRoute.vue'
import DirectRoute from './route/DirectRoute.vue'
import AddRoute from './route-manage/AddRoute.vue'
import DeleteRoute from './route-manage/DeleteRoute.vue'
import AddCity from './city-manage/AddCity.vue'
import DeleteCity from './city-manage/DeleteCity.vue'

export default {
  name: 'TrafficManage',
  components: {
    RouteList,
    OptimalRoute,
    DirectRoute,
    AddRoute,
    DeleteRoute,
    AddCity,
    DeleteCity
  },
  data() {
    return {
      activeTab: 'routeList'
    }
  }
}
</script>

<style>
/* 通用容器样式 */
.container {
  width: 100%;
  min-height: 100vh;
  padding: 20px;
  background-color: #f5f7fa;
}

.wrapper {
  width: 90%;
  max-width: 1200px;
  margin: 0 auto;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  padding: 30px;
}

/* 标题样式 */
.title {
  font-size: 24px;
  color: #303133;
  text-align: center;
  margin-bottom: 30px;
  font-weight: 500;
}

/* 表单样式 */
.form {
  width: 100%;
  max-width: 600px;
  margin: 0 auto;
}

.form .el-form-item {
  margin-bottom: 25px;
}

.form .el-input,
.form .el-select,
.form .el-autocomplete {
  width: 100%;
}

/* 按钮样式 */
.btn-wrapper {
  text-align: center;
  margin-top: 30px;
}

.btn-wrapper .el-button {
  min-width: 120px;
}

/* 列表样式 */
.list {
  width: 100%;
  margin-top: 20px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

/* 搜索框样式 */
.search-wrapper {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.search-wrapper .el-input {
  width: 200px;
}

/* 响应式布局 */
@media screen and (max-width: 768px) {
  .wrapper {
    width: 95%;
    padding: 20px;
  }
  
  .form {
    max-width: 100%;
  }
  
  .search-wrapper {
    flex-direction: column;
  }
  
  .search-wrapper .el-input {
    width: 100%;
  }
} 

/* 导航栏样式 */
.el-tabs__nav-wrap {
  height: 65px;
  padding-left: 40px;
}

.el-tabs__nav {
  padding-left: 20px;
}

.el-tabs__item {
  height: 65px !important;
  line-height: 65px !important;
  font-size: 18px !important;
  font-weight: 500 !important;
  padding: 0 30px !important;
}

.el-tabs__item.is-active {
  font-size: 20px !important;
  font-weight: 600 !important;
}

/* 响应式调整 */
@media screen and (max-width: 768px) {
  .route-list-wrapper {
    width: 95%;
    padding: 20px;
  }

  .form-content {
    padding: 20px;
  }

  .add-route-form .el-form-item__content {
    width: 100%;
  }

  .add-route-title {
    font-size: 28px;
    margin-bottom: 30px;
  }

  .el-tabs__nav-wrap {
    padding-left: 20px;
  }

  .el-tabs__nav {
    padding-left: 10px;
  }

  .el-tabs__item {
    font-size: 16px !important;
    padding: 0 20px !important;
  }

  .el-tabs__item.is-active {
    font-size: 18px !important;
  }
}</style> 