package com.yueqi.ntas.service;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.request.RouteRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GraphServiceTest {

    @Autowired
    private GraphService graphService;

    @BeforeEach
    void setUp() {
        graphService.clear();
        // 添加测试数据
        graphService.addVertex("西安");
        graphService.addVertex("重庆");
        graphService.addVertex("郑州");
        
        // 添加路线
        addTestRoute("西安", "重庆", "火车", "K619", "07:10", "17:37", 98.0);
        addTestRoute("西安", "重庆", "火车", "G1833", "13:16", "18:54", 416.0);
        addTestRoute("西安", "郑州", "火车", "G1914", "06:20", "08:22", 239.0);
        addTestRoute("郑州", "西安", "火车", "G2201", "07:11", "09:31", 239.0);
    }

    private void addTestRoute(String from, String to, String type, String routeNo, 
                            String departure, String arrival, double fare) {
        RouteRequest request = new RouteRequest();
        request.setFromCity(from);
        request.setToCity(to);
        request.setType(type);
        request.setRouteNo(routeNo);
        request.setDeparture(departure);
        request.setArrival(arrival);
        request.setFare(fare);
        graphService.addEdge(request);
    }

    @Test
    void testGetDirectCities() {
        // 测试从西安出发可以直达的城市
        List<String> directCities = graphService.getDirectCities("西安");
        assertNotNull(directCities);
        assertEquals(2, directCities.size());
        assertTrue(directCities.contains("重庆"));
        assertTrue(directCities.contains("郑州"));
        
        // 测试从重庆出发可以直达的城市
        directCities = graphService.getDirectCities("重庆");
        assertNotNull(directCities);
        assertTrue(directCities.isEmpty());
        
        // 测试不存在的城市
        directCities = graphService.getDirectCities("北京");
        assertTrue(directCities.isEmpty());
    }

    @Test
    void testGetDirectRoutesGroupByCity() {
        // 测试从西安出发的所有路线
        Map<String, List<Edge>> routes = graphService.getDirectRoutesGroupByCity("西安");
        assertNotNull(routes);
        assertEquals(2, routes.size());
        
        // 验证到重庆的路线
        List<Edge> toChongqing = routes.get("重庆");
        assertNotNull(toChongqing);
        assertEquals(2, toChongqing.size());
        
        // 验证到郑州的路线
        List<Edge> toZhengzhou = routes.get("郑州");
        assertNotNull(toZhengzhou);
        assertEquals(1, toZhengzhou.size());
        
        // 验证具体路线信息
        Edge route = toChongqing.get(0);
        assertEquals("西安", route.getFromCity());
        assertEquals("重庆", route.getToCity());
        assertEquals("K619", route.getRouteNo());
        assertEquals("07:10", route.getDeparture());
        assertEquals("17:37", route.getArrival());
        assertEquals(98.0, route.getFare());
    }

    @Test
    void testAddAndRemoveVertex() {
        String cityName = "北京";
        graphService.addVertex(cityName);
        List<String> cities = graphService.getAllCities();
        assertTrue(cities.contains(cityName));

        graphService.removeVertex(cityName);
        cities = graphService.getAllCities();
        assertFalse(cities.contains(cityName));
    }

    @Test
    void testAddAndRemoveEdge() {
        RouteRequest request = new RouteRequest();
        request.setFromCity("西安");
        request.setToCity("北京");
        request.setType("火车");
        request.setRouteNo("K123");
        request.setDeparture("08:00");
        request.setArrival("20:00");
        request.setFare(100.0);

        graphService.addVertex("北京");
        graphService.addEdge(request);
        List<Edge> edges = graphService.getDirectEdges(request.getFromCity(), request.getToCity());
        assertFalse(edges.isEmpty());

        graphService.removeEdge(request.getFromCity(), request.getToCity(),
                request.getRouteNo(), request.getDeparture());
        edges = graphService.getDirectEdges(request.getFromCity(), request.getToCity());
        assertTrue(edges.isEmpty());
    }

    @Test
    void testFindShortestPath() {
        String start = "西安";
        String end = "郑州";
        
        // 测试最短时间路径
        List<Edge> timePath = graphService.findShortestPath(start, end, "time");
        assertNotNull(timePath);
        assertFalse(timePath.isEmpty());
        
        // 测试最低费用路径
        List<Edge> costPath = graphService.findShortestPath(start, end, "cost");
        assertNotNull(costPath);
        assertFalse(costPath.isEmpty());
    }
}