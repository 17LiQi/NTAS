package com.yueqi.ntas.service;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import java.util.List;
import java.util.Map;

public interface GraphService {
    void addVertex(String cityName);
    void removeVertex(String cityName);
    void addEdge(RouteRequest request);
    void removeEdge(String from, String to, String routeNo, String departure);
    List<Edge> getAllEdges();
    List<Edge> getDirectEdges(String from, String to);
    List<Edge> findShortestPath(String start, String end, String criterion);
    List<String> getAllCities();
    void clear();
    
    /**
     * 获取从指定城市可以直达的所有城市列表
     * @param fromCity 起始城市
     * @return 可直达城市列表，按字母顺序排序
     */
    List<String> getDirectCities(String fromCity);
    
    /**
     * 获取从指定城市出发的所有直达路线，按目的地城市分组
     * @param fromCity 起始城市
     * @return 按目的地城市分组的路线Map，城市按字母顺序排序
     */
    Map<String, List<Edge>> getDirectRoutesGroupByCity(String fromCity);
    
    /**
     * 查找两个城市间的最短时间路径
     */
    OptimalRouteResponse findShortestPathByTime(String start, String end);
    
    /**
     * 查找两个城市间的最低费用路径
     */
    OptimalRouteResponse findShortestPathByCost(String start, String end);
} 