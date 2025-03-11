package com.yueqi.ntas.service.impl;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.service.RouteQueryService;
import com.yueqi.ntas.service.GraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RouteQueryServiceImpl implements RouteQueryService {
    @Autowired
    private GraphService graphService;

    @Override
    public List<Edge> findAllRoutes() {
        return graphService.getAllEdges();
    }

    @Override
    public List<Edge> findDirectRoutes(String from, String to) {
        return graphService.getDirectEdges(from, to);
    }

    @Override
    public OptimalRouteResponse findOptimalRoute(String start, String end, String criterion) {
        if ("time".equals(criterion)) {
            return graphService.findShortestPathByTime(start, end);
        } else {
            return graphService.findShortestPathByCost(start, end);
        }
    }

    @Override
    public List<String> findDirectCities(String city) {
        return graphService.getDirectCities(city);
    }
} 