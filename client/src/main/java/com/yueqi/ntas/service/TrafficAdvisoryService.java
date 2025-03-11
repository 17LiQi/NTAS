// package com.yueqi.ntas.service;

// import java.util.List;
// import com.yueqi.ntas.domain.Edge;
// import com.yueqi.ntas.domain.RouteRequest;
// import com.yueqi.ntas.domain.entity.Edge;

// public interface TrafficAdvisoryService {
//     // 查询相关
//     List<Edge> findAllRoutes();
//     List<Edge> findDirectRoutes(String from, String to);
//     List<Edge> findOptimalRoute(String start, String end, String criterion);
//     List<String> findAdjacentCities(String fromCity);
//     List<Edge> findDirectRoutesFromCity(String fromCity);
    
//     // 城市管理
//     void addCity(String cityName);
//     void deleteCity(String cityName);
//     boolean cityExists(String cityName);
//     List<String> getAllCities();
    
//     // 路线管理  
//     void addRoute(RouteRequest request);
//     void deleteRoute(String from, String to, String routeNo, String departure);
// }