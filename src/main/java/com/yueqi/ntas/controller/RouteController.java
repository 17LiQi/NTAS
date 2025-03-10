package com.yueqi.ntas.controller;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.response.Result;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.service.RouteQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/traffic/route")
public class RouteController {
    @Autowired
    private RouteQueryService routeQueryService;

    @GetMapping("/direct")
    public Result<List<Edge>> findDirectRoutes(
            @RequestParam String from,
            @RequestParam String to) {
        try {
            List<Edge> routes = routeQueryService.findDirectRoutes(from, to);
            return Result.success(routes);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/optimal")
    public Result<OptimalRouteResponse> findOptimalRoute(
            @RequestParam String start,
            @RequestParam String end,
            @RequestParam String criterion) {
        try {
            OptimalRouteResponse response = routeQueryService.findOptimalRoute(start, end, criterion);
            return Result.success(response);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
} 