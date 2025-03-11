package com.yueqi.ntas.controller;

import com.yueqi.ntas.domain.response.Result;
import com.yueqi.ntas.domain.dto.RouteDTO;
import com.yueqi.ntas.domain.dto.RouteDisplayDTO;
import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.service.RouteQueryService;
import com.yueqi.ntas.util.RouteUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/traffic/route/query")
public class RouteQueryController {
    @Autowired
    private RouteQueryService routeQueryService;

    @GetMapping("/all")
    public Result<List<RouteDTO>> getAllRoutes() {
        try {
            List<Edge> routes = routeQueryService.findAllRoutes();
            return Result.success(convertToRouteDTOs(routes));
        } catch (Exception e) {
            log.error("获取所有路线失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/direct")
    public Result<List<RouteDisplayDTO>> findDirectRoutes(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        try {
            if ((from == null || from.isEmpty()) && (to == null || to.isEmpty())) {
                List<Edge> routes = routeQueryService.findAllRoutes();
                return Result.success(routes.stream()
                        .map(RouteDisplayDTO::new)
                        .collect(Collectors.toList()));
            }

            List<Edge> routes = routeQueryService.findDirectRoutes(from, to);
            return Result.success(routes.stream()
                    .map(RouteDisplayDTO::new)
                    .collect(Collectors.toList()));
        } catch (Exception e) {
            log.error("查询直达路线失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/optimal")
    @ResponseStatus(HttpStatus.OK)
    public Result<?> findOptimalRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam(required = false) String criterion) {
        try {
            OptimalRouteResponse response = routeQueryService.findOptimalRoute(start, end, criterion);
            return Result.success(response);
        } catch (BusinessException e) {
            log.error("查询最优路径失败", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private List<RouteDTO> convertToRouteDTOs(List<Edge> routes) {
        return routes.stream()
                .map(route -> {
                    RouteDTO dto = new RouteDTO();
                    dto.setFromCity(route.getFromCity());
                    dto.setToCity(route.getToCity());
                    dto.setType(route.getType());
                    dto.setRouteNo(route.getRouteNo());
                    dto.setDeparture(route.getDeparture());
                    dto.setArrival(route.getArrival());
                    dto.setFormattedFare(String.format("%.1f 元", route.getFare()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
} 