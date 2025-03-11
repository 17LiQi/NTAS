package com.yueqi.ntas.integration;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.service.GraphService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
public class DirectRouteTest {

    @Autowired
    private GraphService graphService;

    @Test
    void showShanghaiDirectRoutes() {
        String fromCity = "上海";
        System.out.println("\n========== 数据库路线查询 ==========");
        System.out.println("数据库地址: jdbc:mysql://192.168.228.128:3306/ntas");
        
        // 1. 查看所有城市
        System.out.println("\n当前数据库中的所有城市:");
        List<String> allCities = graphService.getAllCities();
        System.out.println("城市总数: " + allCities.size());
        allCities.forEach(city -> System.out.println("- " + city));
        
        // 2. 查看从上海出发的直达城市
        System.out.println("\n=== 从" + fromCity + "出发的直达城市 ===");
        List<String> directCities = graphService.getDirectCities(fromCity);
        System.out.println("直达城市数量: " + directCities.size());
        if (directCities.isEmpty()) {
            System.out.println("警告: 数据库中没有找到从" + fromCity + "出发的直达城市");
        } else {
            directCities.forEach(city -> System.out.println("- " + city));
        }

        // 3. 查看从上海出发的所有路线详情
        System.out.println("\n=== 从" + fromCity + "出发的所有路线 ===");
        Map<String, List<Edge>> routes = graphService.getDirectRoutesGroupByCity(fromCity);
        System.out.println("路线目的地城市数量: " + routes.size());
        
        if (routes.isEmpty()) {
            System.out.println("警告: 数据库中没有找到从" + fromCity + "出发的路线");
        } else {
            int totalRoutes = 0;
            for (List<Edge> edges : routes.values()) {
                totalRoutes += edges.size();
            }
            System.out.println("总路线数量: " + totalRoutes);
            
            routes.forEach((city, edges) -> {
                System.out.println("\n到 " + city + " 的路线 (" + edges.size() + "条):");
                edges.stream()
                     .sorted((e1, e2) -> e1.getDeparture().compareTo(e2.getDeparture()))
                     .forEach(edge -> {
                         // 计算行程时间
                         LocalTime departure = LocalTime.parse(edge.getDeparture());
                         LocalTime arrival = LocalTime.parse(edge.getArrival());
                         long duration = ChronoUnit.MINUTES.between(departure, arrival);
                         long hours = duration / 60;
                         long minutes = duration % 60;
                         
                         System.out.printf("- %s %s %s->%s (用时%d小时%d分钟) 票价%.1f元\n",
                                 edge.getType(),
                                 edge.getRouteNo(),
                                 edge.getDeparture(),
                                 edge.getArrival(),
                                 hours,
                                 minutes,
                                 edge.getFare());
                     });
            });
        }
        
        // 4. 查看到达上海的所有路线
        System.out.println("\n=== 到达" + fromCity + "的所有直达路线 ===");
        boolean foundIncomingRoutes = false;
        int totalIncomingRoutes = 0;
        
        for (String city : allCities) {
            if (city.equals(fromCity)) continue;
            
            List<Edge> incomingRoutes = graphService.getDirectEdges(city, fromCity);
            if (!incomingRoutes.isEmpty()) {
                foundIncomingRoutes = true;
                totalIncomingRoutes += incomingRoutes.size();
                System.out.println("\n从 " + city + " 出发 (" + incomingRoutes.size() + "条):");
                incomingRoutes.stream()
                    .sorted((e1, e2) -> e1.getDeparture().compareTo(e2.getDeparture()))
                    .forEach(edge -> {
                        LocalTime departure = LocalTime.parse(edge.getDeparture());
                        LocalTime arrival = LocalTime.parse(edge.getArrival());
                        long duration = ChronoUnit.MINUTES.between(departure, arrival);
                        
                        System.out.printf("- %s %s %s->%s (用时%d小时%d分钟) 票价%.1f元\n",
                                edge.getType(),
                                edge.getRouteNo(),
                                edge.getDeparture(),
                                edge.getArrival(),
                                duration / 60,
                                duration % 60,
                                edge.getFare());
                    });
            }
        }
        
        if (!foundIncomingRoutes) {
            System.out.println("警告: 数据库中没有找到到达" + fromCity + "的直达路线");
        } else {
            System.out.println("\n总计到达" + fromCity + "的路线数量: " + totalIncomingRoutes);
        }
        
        System.out.println("\n========== 路线信息统计完成 ==========");
    }

    @Test
    void showRoutesToShanghai() {
        String toCity = "上海";
        System.out.println("\n=== 到达" + toCity + "的所有直达路线 ===");
        List<String> allCities = graphService.getAllCities();
        
        boolean foundRoutes = false;
        for (String fromCity : allCities) {
            if (fromCity.equals(toCity)) continue;
            
            List<Edge> routes = graphService.getDirectEdges(fromCity, toCity);
            if (!routes.isEmpty()) {
                foundRoutes = true;
                System.out.println("\n从 " + fromCity + " 出发:");
                routes.forEach(edge -> {
                    LocalTime departure = LocalTime.parse(edge.getDeparture());
                    LocalTime arrival = LocalTime.parse(edge.getArrival());
                    long duration = ChronoUnit.MINUTES.between(departure, arrival);
                    
                    System.out.printf("- %s %s %s->%s (用时%d小时%d分钟) 票价%.1f元\n",
                            edge.getType(),
                            edge.getRouteNo(),
                            edge.getDeparture(),
                            edge.getArrival(),
                            duration / 60,
                            duration % 60,
                            edge.getFare());
                });
            }
        }
        
        if (!foundRoutes) {
            System.out.println("没有找到到达" + toCity + "的直达路线");
        }
    }
} 