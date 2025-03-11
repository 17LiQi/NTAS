package com.yueqi.ntas.integration;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.service.GraphService;
import com.yueqi.ntas.util.RouteUtils;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
public class RouteCalculationTest {

    @Autowired
    private GraphService graphService;

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private RouteMapper routeMapper;

    @BeforeEach
    void setUp() {
        // 验证数据库中是否有数据
        System.out.println("\n=== 验证数据库数据 ===");
        List<City> cities = cityMapper.findAll();
        System.out.println("数据库中的城市数量: " + cities.size());
        if (!cities.isEmpty()) {
            System.out.println("城市列表:");
            cities.forEach(city -> System.out.println("- " + city.getName()));
        }

        // 获取上海和北京的ID
        City shanghai = cityMapper.findByName("上海");
        City beijing = cityMapper.findByName("北京");
        System.out.println("\n上海是否存在: " + (shanghai != null));
        System.out.println("北京是否存在: " + (beijing != null));

        if (shanghai != null && beijing != null) {
            System.out.println("上海ID: " + shanghai.getId());
            System.out.println("北京ID: " + beijing.getId());

            // 验证路线数据
            List<Route> routes = routeMapper.findAll();
            System.out.println("\n数据库中的路线数量: " + routes.size());
            if (!routes.isEmpty()) {
                System.out.println("路线列表:");
                routes.forEach(route -> {
                    // 获取城市名称
                    City fromCity = cityMapper.findById(route.getFromCityId());
                    City toCity = cityMapper.findById(route.getToCityId());
                    if (fromCity != null && toCity != null) {
                        System.out.printf("- %s->%s %s %s %s->%s 票价%.1f元\n",
                            fromCity.getName(), toCity.getName(),
                            route.getTransportType(), route.getRouteNo(),
                            route.getDeparture(), route.getArrival(),
                            route.getFare());
                    }
                });
            }

            // 验证上海到北京的直达路线
            System.out.println("\n上海到北京的直达路线:");
            List<Route> directRoutes = routeMapper.findByFromAndToCity(shanghai.getId(), beijing.getId());
            if (!directRoutes.isEmpty()) {
                directRoutes.forEach(route -> 
                    System.out.printf("- %s %s %s->%s 票价%.1f元\n",
                        route.getTransportType(), route.getRouteNo(),
                        route.getDeparture(), route.getArrival(),
                        route.getFare())
                );
            } else {
                System.out.println("没有直达路线");
            }
        }
    }

    @Test
    void testShortestTimeRoute() {
        System.out.println("\n=== 测试最短时间路径（上海->北京）===");
        
        // 获取城市ID
        City fromCity = cityMapper.findByName("上海");
        City toCity = cityMapper.findByName("北京");
        
        if (fromCity == null || toCity == null) {
            System.out.println("城市不存在");
            return;
        }

        // 获取所有直达城市
        System.out.println("\n从上海可直达的城市：");
        List<Route> fromRoutes = routeMapper.findByFromCity(fromCity.getId());
        List<City> directCities = fromRoutes.stream()
            .map(route -> cityMapper.findById(route.getToCityId()))
            .filter(city -> city != null)
            .distinct()
            .collect(Collectors.toList());
        directCities.forEach(city -> System.out.println("- " + city.getName()));

        // 获取直达路线
        System.out.println("\n从上海出发的所有直达路线：");
        Map<Integer, List<Route>> directRoutes = fromRoutes.stream()
            .collect(Collectors.groupingBy(Route::getToCityId));
        
        directRoutes.forEach((cityId, routes) -> {
            City city = cityMapper.findById(cityId);
            if (city != null) {
                System.out.println("\n到 " + city.getName() + " 的路线:");
                routes.forEach(route -> {
                    System.out.printf("- %s %s %s->%s 票价%.1f元\n",
                        route.getTransportType(), route.getRouteNo(),
                        route.getDeparture(), route.getArrival(),
                        route.getFare());
                    // 打印每条路线的时间计算详情
                    int departureMinutes = timeToMinutes(route.getDeparture().toString());
                    int arrivalMinutes = timeToMinutes(route.getArrival().toString());
                    int duration = calculateDuration(departureMinutes, arrivalMinutes);
                    System.out.printf("  出发时间: %s (%d分钟), 到达时间: %s (%d分钟), 行程时间: %d分钟\n",
                        route.getDeparture(), departureMinutes,
                        route.getArrival(), arrivalMinutes,
                        duration);
                });
            }
        });

        // 测试最短时间路径
        System.out.println("\n计算最短时间路径：");
        List<Route> timeRoutes = routeMapper.findOptimalRoute(fromCity.getId(), toCity.getId(), "time");
        if (timeRoutes.isEmpty()) {
            System.out.println("未找到可行路线");
            return;
        }

        System.out.println("\n最短时间路径详情：");
        long totalMinutes = 0;
        double totalFare = 0;
        Route prevRoute = null;

        for (Route route : timeRoutes) {
            // 打印路线基本信息
            City currentFromCity = cityMapper.findById(route.getFromCityId());
            City currentToCity = cityMapper.findById(route.getToCityId());
            
            if (currentFromCity != null && currentToCity != null) {
                System.out.printf("\n- %s->%s %s %s %s->%s 票价%.1f元\n",
                    currentFromCity.getName(), currentToCity.getName(),
                    route.getTransportType(), route.getRouteNo(),
                    route.getDeparture(), route.getArrival(),
                    route.getFare());
                
                // 计算并打印时间详情
                int departureMinutes = timeToMinutes(route.getDeparture().toString());
                int arrivalMinutes = timeToMinutes(route.getArrival().toString());
                int duration = calculateDuration(departureMinutes, arrivalMinutes);
                
                System.out.printf("  行程时间计算:\n");
                System.out.printf("  - 出发时间: %s (%d分钟)\n", route.getDeparture(), departureMinutes);
                System.out.printf("  - 到达时间: %s (%d分钟)\n", route.getArrival(), arrivalMinutes);
                System.out.printf("  - 行程时间: %d分钟\n", duration);
                
                // 如果不是第一段路程，计算等待时间
                if (prevRoute != null) {
                    int prevArrivalMinutes = timeToMinutes(prevRoute.getArrival().toString());
                    int waitTime = calculateWaitTime(prevArrivalMinutes, departureMinutes);
                    System.out.printf("  - 等待时间: %d分钟\n", waitTime);
                    totalMinutes += waitTime;
                }
                
                totalMinutes += duration;
                totalFare += route.getFare().doubleValue();
                prevRoute = route;
            }
        }

        System.out.printf("\n总时间：%d小时%d分钟\n", totalMinutes / 60, totalMinutes % 60);
        System.out.printf("总费用：%.1f元\n", totalFare);
    }

    @Test
    void testLeastCostRoute() {
        System.out.println("\n=== 测试最少费用路径（上海->北京）===");
        
        // 获取城市ID
        City fromCity = cityMapper.findByName("上海");
        City toCity = cityMapper.findByName("北京");
        
        if (fromCity == null || toCity == null) {
            System.out.println("城市不存在");
            return;
        }

        // 测试最少费用路径
        System.out.println("\n计算最少费用路径：");
        List<Route> costRoutes = routeMapper.findOptimalRoute(fromCity.getId(), toCity.getId(), "cost");
        if (costRoutes.isEmpty()) {
            System.out.println("未找到可行路线");
            return;
        }

        System.out.println("\n最少费用路径详情：");
        long totalMinutes = 0;
        double totalFare = 0;
        Route prevRoute = null;

        for (Route route : costRoutes) {
            // 打印路线基本信息
            City currentFromCity = cityMapper.findById(route.getFromCityId());
            City currentToCity = cityMapper.findById(route.getToCityId());
            
            if (currentFromCity != null && currentToCity != null) {
                System.out.printf("\n- %s->%s %s %s %s->%s 票价%.1f元\n",
                    currentFromCity.getName(), currentToCity.getName(),
                    route.getTransportType(), route.getRouteNo(),
                    route.getDeparture(), route.getArrival(),
                    route.getFare());
                
                // 计算并打印时间详情
                int departureMinutes = timeToMinutes(route.getDeparture().toString());
                int arrivalMinutes = timeToMinutes(route.getArrival().toString());
                int duration = calculateDuration(departureMinutes, arrivalMinutes);
                
                System.out.printf("  行程时间计算:\n");
                System.out.printf("  - 出发时间: %s (%d分钟)\n", route.getDeparture(), departureMinutes);
                System.out.printf("  - 到达时间: %s (%d分钟)\n", route.getArrival(), arrivalMinutes);
                System.out.printf("  - 行程时间: %d分钟\n", duration);
                
                // 如果不是第一段路程，计算等待时间
                if (prevRoute != null) {
                    int prevArrivalMinutes = timeToMinutes(prevRoute.getArrival().toString());
                    int waitTime = calculateWaitTime(prevArrivalMinutes, departureMinutes);
                    System.out.printf("  - 等待时间: %d分钟\n", waitTime);
                    totalMinutes += waitTime;
                }
                
                totalMinutes += duration;
                totalFare += route.getFare().doubleValue();
                prevRoute = route;
            }
        }

        System.out.printf("\n总时间：%d小时%d分钟\n", totalMinutes / 60, totalMinutes % 60);
        System.out.printf("总费用：%.1f元\n", totalFare);
    }

    private int timeToMinutes(String time) {
        String[] parts = time.split(":");
        return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
    }

    private int calculateDuration(int departureMinutes, int arrivalMinutes) {
        if (arrivalMinutes < departureMinutes) {  // 跨天的情况
            return (24 * 60 - departureMinutes) + arrivalMinutes;
        } else {
            return arrivalMinutes - departureMinutes;
        }
    }

    private int calculateWaitTime(int prevArrivalMinutes, int nextDepartureMinutes) {
        if (nextDepartureMinutes < prevArrivalMinutes) {
            // 需要等到第二天
            return (24 * 60 - prevArrivalMinutes) + nextDepartureMinutes;
        } else {
            return nextDepartureMinutes - prevArrivalMinutes;
        }
    }
} 