package com.yueqi.ntas.util;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.domain.dto.RouteDisplayDTO;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RouteUtils {
    
    public static OptimalRouteResponse calculateRouteDetails(List<Edge> routes) {
        if (routes == null || routes.isEmpty()) {
            return OptimalRouteResponse.builder()
                    .routes(new ArrayList<>())
                    .totalTime("0小时0分钟")
                    .totalFare(0.0)
                    .totalMinutes(0)
                    .build();
        }

        // 计算总时间（包括等待时间）和总费用
        long totalMinutes = calculateTotalTime(routes);
        double totalCost = routes.stream()
                .mapToDouble(Edge::getFare)
                .sum();

        return buildResponse(routes, totalMinutes, totalCost);
    }

    public static OptimalRouteResponse calculateRouteDetails(List<Edge> routes, long totalMinutes, double totalCost) {
        return buildResponse(routes, totalMinutes, totalCost);
    }

    private static long calculateTotalTime(List<Edge> routes) {
        if (routes.isEmpty()) return 0;
        
        long totalMinutes = 0;
        LocalTime lastArrival = null;

        for (Edge edge : routes) {
            LocalTime departure = LocalTime.parse(edge.getDeparture());
            LocalTime arrival = LocalTime.parse(edge.getArrival());
            
            // 计算行程时间
            long travelMinutes;
            if (arrival.isBefore(departure)) {
                // 如果到达时间早于出发时间，说明跨天了
                travelMinutes = ChronoUnit.MINUTES.between(departure, LocalTime.of(24, 0)) +
                              ChronoUnit.MINUTES.between(LocalTime.of(0, 0), arrival);
            } else {
                travelMinutes = ChronoUnit.MINUTES.between(departure, arrival);
            }
            totalMinutes += travelMinutes;
            
            // 计算等待时间
            if (lastArrival != null) {
                long waitMinutes;
                if (departure.isBefore(lastArrival)) {
                    // 如果下一班出发时间早于上一班到达时间，说明要等到第二天
                    waitMinutes = ChronoUnit.MINUTES.between(lastArrival, LocalTime.of(24, 0)) +
                                ChronoUnit.MINUTES.between(LocalTime.of(0, 0), departure);
                } else {
                    waitMinutes = ChronoUnit.MINUTES.between(lastArrival, departure);
                }
                totalMinutes += waitMinutes;
            }
            
            lastArrival = arrival;
        }

        return totalMinutes;
    }

    private static int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private static OptimalRouteResponse buildResponse(List<Edge> routes, long totalMinutes, double totalCost) {
        // 转换总时间为可读格式
        long hours = totalMinutes / 60;
        long minutes = totalMinutes % 60;

        StringBuilder timeBuilder = new StringBuilder();
        if (hours >= 24) {
            long days = hours / 24;
            hours = hours % 24;
            timeBuilder.append(days).append("天");
        }
        timeBuilder.append(hours).append("小时");
        timeBuilder.append(minutes).append("分钟");

        // 转换路线为显示DTO
        List<RouteDisplayDTO> displayRoutes = routes.stream()
                .map(RouteDisplayDTO::new)
                .collect(Collectors.toList());

        return OptimalRouteResponse.builder()
                .routes(displayRoutes)
                .totalTime(timeBuilder.toString())
                .totalFare(totalCost)
                .totalMinutes((int)totalMinutes)
                .build();
    }
} 