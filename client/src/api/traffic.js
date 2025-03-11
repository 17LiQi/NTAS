import request from '@/utils/request'

// 获取所有城市
export function getCities(query) {
  return request({
    url: '/traffic/city',
    method: 'get',
    params: { query }
  })
}

// 获取所有路线
export function getAllRoutes() {
  return request({
    url: '/traffic/route/query/all',
    method: 'get'
  })
}

// 获取直达路线
export function getDirectRoutes(from, to) {
  return request({
    url: '/traffic/route/query/direct',
    method: 'get',
    params: { from, to }
  })
}

// 获取最优路线
export function getOptimalRoute(start, end, criterion) {
  return request({
    url: '/traffic/route/query/optimal',
    method: 'get',
    params: {
      start,
      end,
      criterion
    }
  })
}

// 获取相邻城市
export function getAdjacentCities(city) {
  return request({
    url: '/traffic/city/adjacent',
    method: 'get',
    params: { fromCity: city }
  })
}

// 添加路线
export function addRoute(data) {
  return request({
    url: '/traffic/route',
    method: 'post',
    data
  })
}

// 删除路线
export function deleteRoute(data) {
  return request({
    url: '/traffic/route/delete',
    method: 'post',
    data: {
      fromCity: data.fromCity,
      toCity: data.toCity,
      routeNo: data.routeNo,
      departure: data.departure
    }
  })
}

// 添加城市
export function addCity(name) {
  return request({
    url: '/traffic/city',
    method: 'post',
    params: {
      cityName: name
    }
  })
}

// 删除城市
export function deleteCity(name) {
  return request({
    url: '/traffic/city/delete',
    method: 'post',
    params: {
      cityName: name
    }
  })
}

// 验证城市
export function validateCity(name) {
  return request({
    url: '/traffic/city/validate',
    method: 'get',
    params: { cityName: name }
  })
}

// 获取路线删除信息
export function getRouteDeleteInfo(fromCity, toCity, routeNo, departure) {
  return request({
    url: '/traffic/route/delete/info',
    method: 'get',
    params: {
      fromCity,
      toCity,
      type: routeNo.charAt(0) === 'G' ? '火车' : '飞机',
      routeNo
    }
  })
}

// 获取路线删除确认信息
export function getRouteDeleteConfirm(fromCity, toCity, type, routeNo) {
  return request({
    url: '/traffic/route/delete/confirm',
    method: 'get',
    params: {
      fromCity,
      toCity,
      type,
      routeNo
    }
  })
}

// 获取城市删除信息
export function getCityDeleteInfo(name) {
  return request({
    url: '/traffic/city/delete',
    method: 'post',
    params: {
      cityName: name
    }
  })
}

