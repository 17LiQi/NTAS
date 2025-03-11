import { createRouter, createWebHashHistory } from 'vue-router'
import Home from '../views/Home.vue'
import TrafficManage from '../components/traffic/TrafficManage.vue'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/home',
    redirect: '/'
  },
  {
    path: '/traffic',
    name: 'TrafficManage',
    component: TrafficManage
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHashHistory(process.env.BASE_URL),
  routes
})

router.beforeEach((to, from, next) => {
  if (to.path === '/traffic') {
    next()
  } else {
    next()
  }
})

export default router 