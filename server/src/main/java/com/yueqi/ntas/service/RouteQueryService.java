package com.yueqi.ntas.service;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import java.util.List;

public interface RouteQueryService {
    /**
     * 获取所有路线。
     *
     * @return 所有路线的列表
     */
    List<Edge> findAllRoutes();

    /**
     * 根据起点和终点查找直接路线。
     *
     * @param from 起点城市名称
     * @param to 终点城市名称
     * @return 直接路线的列表
     */
    List<Edge> findDirectRoutes(String from, String to);

    /**
     * 根据起点、终点和标准查找最优路线。
     */
    OptimalRouteResponse findOptimalRoute(String start, String end, String criterion);

    /**
     * 查找指定城市的所有直达城市
     * @param city 起始城市
     * @return 可直达城市列表
     */
    List<String> findDirectCities(String city);
}
