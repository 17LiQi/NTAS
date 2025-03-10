package com.yueqi.ntas.service.impl;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.util.RouteUtils;
import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import com.yueqi.ntas.service.GraphService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
public class GraphServiceImpl implements GraphService {
    @Autowired
    private RouteMapper routeMapper;
    
    @Autowired
    private CityMapper cityMapper;

    // 使用邻接表存储图结构，改用HashMap提高查找效率
    private final Map<String, Map<String, Set<Edge>>> adjacencyList = new HashMap<>();
    
    // 缓存优化：预计算常用路径
    private final Map<String, Map<String, Map<String, List<Edge>>>> pathCache = new HashMap<>();
    
    // 缓存城市信息避免重复查询数据库
    private final Map<Long, City> cityCache = new HashMap<>();

    @PostConstruct
    public void init() {
        // 减少日志输出频率
        log.info("初始化图数据开始");
        try {
            loadDataFromDatabase();
            // 只在初始化完成时输出一次日志
            if(log.isDebugEnabled()) {
                log.debug("图数据初始化完成，当前包含 {} 个城市", adjacencyList.size());
            }
        } catch (Exception e) {
            log.error("初始化图数据失败: {}", e.getMessage());
        }
    }

    private void loadDataFromDatabase() {
        try {
            List<City> cities = cityMapper.findAll();
            List<Route> routes = routeMapper.findAll();
            
            if(log.isDebugEnabled()) {
                log.debug("加载数据：{} 个城市, {} 条路线", cities.size(), routes.size());
            }
            
            // 清空现有数据
            adjacencyList.clear();
            pathCache.clear();
            cityCache.clear();
            
            // 初始化城市缓存
            for (City city : cities) {
                if (city != null && city.getName() != null) {
                    cityCache.put(city.getId().longValue(), city);
                    adjacencyList.putIfAbsent(city.getName(), new HashMap<>());
                }
            }
            
            // 批量加载路线
            for (Route route : routes) {
                if (route == null) continue;
                
                City fromCity = cityCache.get(route.getFromCityId().longValue());
                City toCity = cityCache.get(route.getToCityId().longValue());
                if (fromCity == null || toCity == null) continue;
                
                try {
                    Edge edge = Edge.builder()
                        .fromCity(fromCity.getName())
                        .toCity(toCity.getName())
                        .type(route.getTransportType())
                        .routeNo(route.getRouteNo())
                        .departure(route.getDeparture().toString())
                        .arrival(route.getArrival().toString())
                        .fare(route.getFare().doubleValue())
                        .build();
                    
                    addEdgeToGraph(edge);
                } catch (Exception e) {
                    if(log.isDebugEnabled()) {
                        log.debug("跳过无效路线: {}", route);
                    }
                }
            }
        } catch (Exception e) {
            throw new BusinessException("加载数据失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void addVertex(String cityName) {
        try {
            if(log.isDebugEnabled()) {
                log.debug("添加城市: {}", cityName);
            }
            
            if (adjacencyList.containsKey(cityName)) {
                throw new BusinessException("城市已存在");
            }
            
            // 添加到图结构
            adjacencyList.put(cityName, new HashMap<>());
            
            // 保存到数据库
            if (cityMapper.findByName(cityName) == null) {
                City city = new City();
                city.setName(cityName);
                cityMapper.insert(city);
            }
            
            if(log.isDebugEnabled()) {
                log.debug("城市添加成功: {}", cityName);
            }
        } catch (Exception e) {
            log.error("添加城市失败: {}", cityName, e);
            throw new BusinessException("添加城市失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeVertex(String cityName) {
        try {
            if(log.isDebugEnabled()) {
                log.debug("删除城市: {}", cityName);
            }
            
            if (!adjacencyList.containsKey(cityName)) {
                throw new BusinessException("城市不存在");
            }

            // 从图中删除
            adjacencyList.remove(cityName);
            
            // 删除其他城市到该城市的路线
            adjacencyList.values().forEach(cityEdges -> cityEdges.remove(cityName));
            
            // 清除路径缓存
            pathCache.values().forEach(startMap -> 
                startMap.values().forEach(endMap -> 
                    endMap.remove(cityName)));

            // 从数据库删除
            City city = cityMapper.findByName(cityName);
            if (city != null) {
                List<Route> routes = routeMapper.findByFromCity(city.getId());
                for (Route route : routes) {
                    routeMapper.delete(route);
                }
                cityMapper.deleteByName(cityName);
            }
            
            if(log.isDebugEnabled()) {
                log.debug("城市删除成功: {}", cityName);
            }
        } catch (Exception e) {
            log.error("删除城市失败: {}", cityName, e);
            throw new BusinessException("删除城市失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void addEdge(RouteRequest request) {
        try {
            if(log.isDebugEnabled()) {
                log.debug("添加路线: {} -> {}", request.getFromCity(), request.getToCity());
            }
            
            validateCities(request.getFromCity(), request.getToCity());
            
            // 创建边
            Edge edge = Edge.builder()
                    .fromCity(request.getFromCity())
                    .toCity(request.getToCity())
                    .type(request.getType())
                    .routeNo(request.getRouteNo())
                    .departure(request.getDeparture())
                    .arrival(request.getArrival())
                    .fare(request.getFare())
                    .build();

            // 添加到图
            addEdgeToGraph(edge);

            // 保存到数据库
            Route route = new Route();
            City fromCity = cityMapper.findByName(request.getFromCity());
            City toCity = cityMapper.findByName(request.getToCity());
            route.setFromCityId(fromCity.getId());
            route.setToCityId(toCity.getId());
            route.setTransportType(request.getType());
            route.setRouteNo(request.getRouteNo());
            route.setDeparture(LocalTime.parse(request.getDeparture()));
            route.setArrival(LocalTime.parse(request.getArrival()));
            route.setFare(BigDecimal.valueOf(request.getFare()));
            routeMapper.insert(route);
            
            if(log.isDebugEnabled()) {
                log.debug("路线添加成功");
            }
        } catch (Exception e) {
            log.error("添加路线失败", e);
            throw new BusinessException("添加路线失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void removeEdge(String from, String to, String routeNo, String departure) {
        try {
            log.info("开始删除路线: {} -> {}, 路线号: {}", from, to, routeNo);
            validateCities(from, to);

            // 从图中删除
            Map<String, Set<Edge>> fromEdges = adjacencyList.get(from);
            if (fromEdges != null) {
                Set<Edge> edges = fromEdges.get(to);
                if (edges != null) {
                    edges.removeIf(edge -> 
                        edge.getRouteNo().equals(routeNo) && 
                        edge.getDeparture().equals(departure));
                }
            }

            // 清除相关路径缓存
            pathCache.values().forEach(startMap -> 
                startMap.values().forEach(endMap -> 
                    endMap.remove(to)));

            // 从数据库删除
            City fromCity = cityMapper.findByName(from);
            City toCity = cityMapper.findByName(to);
            if (fromCity != null && toCity != null) {
                Route route = Route.builder()
                    .fromCityId(fromCity.getId())
                    .toCityId(toCity.getId())
                    .routeNo(routeNo)
                    .departure(LocalTime.parse(departure))
                    .build();
                routeMapper.delete(route);
            }
            
            if(log.isDebugEnabled()) {
                log.debug("路线删除成功");
            }
        } catch (Exception e) {
            log.error("删除路线失败", e);
            throw new BusinessException("删除路线失败: " + e.getMessage());
        }
    }

    @Override
    public List<Edge> getAllEdges() {
        return adjacencyList.values().stream()
                .flatMap(map -> map.values().stream())
                .flatMap(Set::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Edge> getDirectEdges(String from, String to) {
        if (from == null || !adjacencyList.containsKey(from)) {
            return new ArrayList<>();
        }
        
        Map<String, Set<Edge>> cityEdges = adjacencyList.get(from);
        if (cityEdges == null) {
            return new ArrayList<>();
        }
        
        if (to != null) {
            Set<Edge> directEdges = cityEdges.get(to);
            return directEdges != null ? new ArrayList<>(directEdges) : new ArrayList<>();
        }
        
        return cityEdges.values().stream()
                .flatMap(Set::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Edge> findShortestPath(String start, String end, String criterion) {
        if (!adjacencyList.containsKey(start) || !adjacencyList.containsKey(end)) {
            return Collections.emptyList();
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, Edge> previousEdges = new HashMap<>();
        Map<String, LocalTime> arrivalTimes = new HashMap<>();
        Set<String> visited = new HashSet<>();

        // 使用TreeMap优化优先队列性能
        TreeMap<Double, String> queue = new TreeMap<>();
        
        // 初始化
        for (String city : adjacencyList.keySet()) {
            distances.put(city, Double.MAX_VALUE);
            arrivalTimes.put(city, null);
        }
        distances.put(start, 0.0);
        arrivalTimes.put(start, LocalTime.MIN);
        queue.put(0.0, start);

        while (!queue.isEmpty()) {
            String current = queue.pollFirstEntry().getValue();
            if (current.equals(end)) break;
            
            if (visited.contains(current)) continue;
            visited.add(current);

            Map<String, Set<Edge>> edges = adjacencyList.get(current);
            if (edges == null) continue;

            for (Map.Entry<String, Set<Edge>> entry : edges.entrySet()) {
                String next = entry.getKey();
                if (visited.contains(next)) continue;

                for (Edge edge : entry.getValue()) {
                    // 获取当前到达时间
                    LocalTime currentArrival = arrivalTimes.get(current);
                    LocalTime edgeDeparture = LocalTime.parse(edge.getDeparture());
                    LocalTime edgeArrival = LocalTime.parse(edge.getArrival());
                    
                    // 计算等待时间（分钟）
                    long waitTime = 0;
                    if (currentArrival != null && !current.equals(start)) {
                        if (edgeDeparture.isBefore(currentArrival)) {
                            // 如果出发时间早于到达时间，说明需要等到第二天
                            waitTime = ChronoUnit.MINUTES.between(currentArrival, LocalTime.of(23, 59)) + 1 +
                                     ChronoUnit.MINUTES.between(LocalTime.of(0, 0), edgeDeparture);
                        } else {
                            waitTime = ChronoUnit.MINUTES.between(currentArrival, edgeDeparture);
                        }
                    }

                    // 计算行程时间（分钟）
                    long travelTime;
                    if (edgeArrival.isBefore(edgeDeparture)) {
                        // 跨天的情况
                        travelTime = ChronoUnit.MINUTES.between(edgeDeparture, LocalTime.of(23, 59)) + 1 +
                                   ChronoUnit.MINUTES.between(LocalTime.of(0, 0), edgeArrival);
                    } else {
                        travelTime = ChronoUnit.MINUTES.between(edgeDeparture, edgeArrival);
                    }

                    // 根据决策原则计算权重
                    double weight;
                    if ("time".equals(criterion)) {
                        weight = waitTime + travelTime;
                    } else {
                        weight = edge.getFare();
                    }
                    
                    double newDistance = distances.get(current) + weight;
                    if (newDistance < distances.get(next)) {
                        distances.put(next, newDistance);
                        previousEdges.put(next, edge);
                        arrivalTimes.put(next, edgeArrival);
                        queue.put(newDistance, next);
                    }
                }
            }
        }

        return reconstructPath(start, end, previousEdges);
    }

    private List<Edge> reconstructPath(String start, String end, Map<String, Edge> previousEdges) {
        List<Edge> path = new ArrayList<>();
        String current = end;
        
        while (!current.equals(start)) {
            Edge edge = previousEdges.get(current);
            if (edge == null) break;
            path.add(0, edge);
            current = edge.getFromCity();
        }
        
        return path;
    }

    private void addEdgeToGraph(Edge edge) {
        adjacencyList.computeIfAbsent(edge.getFromCity(), k -> new HashMap<>())
                    .computeIfAbsent(edge.getToCity(), k -> new HashSet<>())
                    .add(edge);
    }

    @Override
    public List<String> getAllCities() {
        return new ArrayList<>(adjacencyList.keySet());
    }

    @Override
    public void clear() {
        adjacencyList.clear();
        pathCache.clear();
        cityCache.clear();
        log.info("已清除内存中的图数据结构");
    }

    private void validateCities(String... cities) {
        for (String city : cities) {
            if (city != null && !adjacencyList.containsKey(city)) {
                throw new BusinessException("城市不存在: " + city);
            }
        }
    }

    private Edge convertToEdge(Route route, City fromCity, City toCity) {
        return Edge.builder()
            .fromCity(fromCity.getName())
            .toCity(toCity.getName())
            .type(route.getTransportType())
            .routeNo(route.getRouteNo())
            .departure(route.getDeparture().toString())
            .arrival(route.getArrival().toString())
            .fare(route.getFare().doubleValue())
            .build();
    }

    @Override
    public OptimalRouteResponse findShortestPathByTime(String start, String end) {
        List<Edge> routes = findShortestPath(start, end, "time");
        return calculateRouteResponse(routes);
    }

    @Override
    public OptimalRouteResponse findShortestPathByCost(String start, String end) {
        List<Edge> routes = findShortestPath(start, end, "cost");
        return calculateRouteResponse(routes);
    }

    private OptimalRouteResponse calculateRouteResponse(List<Edge> routes) {
        if (routes.isEmpty()) {
            return RouteUtils.calculateRouteDetails(routes);
        }

        // 计算总时间（包括等待时间）
        long totalMinutes = 0;
        double totalCost = 0;

        LocalTime lastArrival = null;
        for (Edge edge : routes) {
            // 计算行程时间
            LocalTime departure = LocalTime.parse(edge.getDeparture());
            LocalTime arrival = LocalTime.parse(edge.getArrival());
            long travelMinutes;
            if (arrival.isBefore(departure)) {
                // 如果到达时间早于出发时间，说明跨天了
                travelMinutes = ChronoUnit.MINUTES.between(departure, LocalTime.of(23, 59)) + 1 +
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
                    waitMinutes = ChronoUnit.MINUTES.between(lastArrival, LocalTime.of(23, 59)) + 1 +
                                ChronoUnit.MINUTES.between(LocalTime.of(0, 0), departure);
                } else {
                    waitMinutes = ChronoUnit.MINUTES.between(lastArrival, departure);
                }
                totalMinutes += waitMinutes;
            }

            lastArrival = arrival;
            totalCost += edge.getFare();
        }

        return RouteUtils.calculateRouteDetails(routes, totalMinutes, totalCost);
    }

    @Override
    public List<String> getDirectCities(String fromCity) {
        if (fromCity == null || !adjacencyList.containsKey(fromCity)) {
            return Collections.emptyList();
        }
        
        Map<String, Set<Edge>> cityEdges = adjacencyList.get(fromCity);
        if (cityEdges == null) {
            return Collections.emptyList();
        }
        
        // 获取所有直达城市，并按字母顺序排序
        return cityEdges.keySet().stream()
                .sorted()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<Edge>> getDirectRoutesGroupByCity(String fromCity) {
        if (fromCity == null || !adjacencyList.containsKey(fromCity)) {
            return Collections.emptyMap();
        }
        
        Map<String, Set<Edge>> cityEdges = adjacencyList.get(fromCity);
        if (cityEdges == null) {
            return Collections.emptyMap();
        }
        
        // 转换Set为List并返回
        return cityEdges.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> new ArrayList<>(e.getValue()),
                    (v1, v2) -> v1,
                    TreeMap::new  // 使用TreeMap保证城市按字母顺序排序
                ));
    }
} 