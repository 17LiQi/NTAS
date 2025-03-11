//package com.yueqi.ntas;
//
//import com.yueqi.ntas.domain.entity.Edge;
//import com.yueqi.ntas.domain.request.RouteRequest;
//import com.yueqi.ntas.service.*;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.junit.jupiter.api.AfterEach;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.ArrayList;
//import java.util.stream.Collectors;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class RouteQueryIntegrationTest {
//
//    private static final Logger log = LoggerFactory.getLogger(RouteQueryIntegrationTest.class);
//
//    @Autowired
//    private RouteQueryService routeQueryService;
//
//    @Autowired
//    private RouteManageService routeManageService;
//
//    @Autowired
//    private GraphService graphService;
//
//    private List<String> testRouteNos = new ArrayList<>();
//
//    @BeforeEach
//    void setUp() {
//        // 清空之前的测试数据
//        clearTestRoutes();
//    }
//
//    private void clearTestRoutes() {
//        try {
//            // 只删除测试路线
//            for (String routeNo : testRouteNos) {
//                List<Edge> edges = graphService.getAllEdges().stream()
//                    .filter(edge -> edge.getRouteNo().equals(routeNo))
//                    .collect(Collectors.toList());
//
//                for (Edge edge : edges) {
//                    routeManageService.deleteRoute(
//                        edge.getFromCity(),
//                        edge.getToCity(),
//                        edge.getRouteNo(),
//                        edge.getDeparture()
//                    );
//                }
//            }
//            testRouteNos.clear();
//        } catch (Exception e) {
//            log.warn("清理测试路线数据时发生异常", e);
//        }
//    }
//
//    private void prepareTestRoute(String from, String to, String routeNo,
//            String departure, String arrival, double fare) {
//        // 检查路线是否已存在
//        List<Edge> existingRoutes = graphService.getDirectEdges(from, to);
//        boolean routeExists = existingRoutes.stream()
//                .anyMatch(route -> route.getRouteNo().equals(routeNo));
//
//        if (!routeExists) {
//            RouteRequest request = new RouteRequest();
//            request.setFromCity(from);
//            request.setToCity(to);
//            request.setType("火车");
//            request.setRouteNo(routeNo);
//            request.setDeparture(departure);
//            request.setArrival(arrival);
//            request.setFare(fare);
//
//            routeManageService.addRoute(request);
//            testRouteNos.add(routeNo);  // 记录测试路线编号
//            log.info("添加测试路线: {} -> {}, 路线号: {}", from, to, routeNo);
//        } else {
//            log.info("测试路线已存在: {} -> {}, 路线号: {}", from, to, routeNo);
//            // 如果路线已存在，也需要记录到测试路线中，以便测试结束时清理
//            testRouteNos.add(routeNo);
//        }
//    }
//
//    @AfterEach
//    void tearDown() {
//        clearTestRoutes();  // 清理测试数据
//    }
//
//    @Test
//    void testFindAllRoutes() {
//        List<Edge> allRoutes = routeQueryService.findAllRoutes();
//        assertFalse(allRoutes.isEmpty(), "应该找到所有路线");
//        assertTrue(allRoutes.size() >= 3, "应该至少有3条测试路线");
//    }
//
//    @Test
//    void testFindDirectRoutes() {
//        List<Edge> directRoutes = routeQueryService.findDirectRoutes("西安", "北京");
//        assertFalse(directRoutes.isEmpty(), "应该找到直达路线");
//        assertEquals("K123", directRoutes.get(0).getRouteNo(), "应该找到正确的路线");
//    }
//
//    @Test
//    void testFindOptimalRouteByCost() {
//        // 准备测试数据：直达路线费用高，中转路线费用低
//        prepareTestRoute("西安", "上海", "K125", "10:00", "22:00", 300.0);  // 直达
//        prepareTestRoute("西安", "北京", "K123", "08:00", "15:00", 100.0);  // 中转1
//        prepareTestRoute("北京", "上海", "K124", "16:00", "21:00", 100.0);  // 中转2
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "cost");
//        assertNotNull(optimalRoute, "路线不应该为null");
//        assertFalse(optimalRoute.isEmpty(), "应该找到最优路线");
//
//        // 验证是否选择了费用最低的路线（中转路线）
//        double totalFare = optimalRoute.stream()
//                .mapToDouble(Edge::getFare)
//                .sum();
//        assertEquals(200.0, totalFare, "应该选择总费用为200的中转路线");
//        assertEquals(2, optimalRoute.size(), "应该是两段路线组成的中转路线");
//    }
//
//    @Test
//    void testFindOptimalRouteByTime() {
//        // 准备测试数据：直达路线时间短，中转路线时间长
//        prepareTestRoute("西安", "上海", "K125", "10:00", "18:00", 300.0);  // 直达8小时
//        prepareTestRoute("西安", "北京", "K123", "08:00", "15:00", 100.0);  // 中转1 7小时
//        prepareTestRoute("北京", "上海", "K124", "16:00", "23:00", 100.0);  // 中转2 7小时
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "time");
//        assertNotNull(optimalRoute, "路线不应该为null");
//        assertFalse(optimalRoute.isEmpty(), "应该找到最优路线");
//
//        // 验证是否选择了时间最短的路线（直达路线）
//        assertEquals(1, optimalRoute.size(), "应该选择直达路线");
//        Edge route = optimalRoute.get(0);
//        assertEquals("K125", route.getRouteNo(), "应该选择直达路线K125");
//        assertEquals(8, route.calculateDuration() / 60, "直达路线应该是8小时");
//    }
//
//    @Test
//    void testFindNoRouteAvailable() {
//        List<Edge> routes = routeQueryService.findDirectRoutes("西安", "广州");
//        assertTrue(routes.isEmpty(), "不存在的路线应该返回空列表");
//    }
//
//    @Test
//    void testFindOptimalRouteWithNoPath() {
//        // 测试不存在路径的情况
//        List<Edge> route = routeQueryService.findOptimalRoute("西安", "广州", "cost");
//        assertTrue(route.isEmpty(), "不存在的路径应该返回空列表");
//    }
//
//    @Test
//    void testFindOptimalRouteWithMultiplePaths() {
//        // 准备测试数据：多条可选路径
//        prepareTestRoute("西安", "上海", "K125", "10:00", "22:00", 300.0);  // 直达
//        prepareTestRoute("西安", "郑州", "K126", "08:00", "12:00", 80.0);   // 路径1-1
//        prepareTestRoute("郑州", "上海", "K127", "13:00", "18:00", 100.0);  // 路径1-2
//        prepareTestRoute("西安", "北京", "K128", "09:00", "15:00", 90.0);   // 路径2-1
//        prepareTestRoute("北京", "上海", "K129", "16:00", "20:00", 120.0);  // 路径2-2
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "cost");
//        assertNotNull(optimalRoute);
//        assertFalse(optimalRoute.isEmpty());
//
//        // 验证是否选择了总费用最低的路径（西安-郑州-上海）
//        double totalFare = optimalRoute.stream()
//                .mapToDouble(Edge::getFare)
//                .sum();
//        assertEquals(180.0, totalFare, "应该选择总费用为180的路线");
//        assertEquals(2, optimalRoute.size(), "应该是两段路线");
//    }
//
//    @Test
//    void testFindOptimalRouteWithTimeConstraint() {
//        // 准备测试数据：考虑时间约束的路径
//        prepareTestRoute("西安", "上海", "K125", "10:00", "18:00", 300.0);  // 直达8小时
//        prepareTestRoute("西安", "郑州", "K126", "08:00", "12:00", 80.0);   // 4小时
//        prepareTestRoute("郑州", "上海", "K127", "13:00", "18:00", 100.0);  // 5小时+1小时等待
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "time");
//        assertNotNull(optimalRoute);
//        assertFalse(optimalRoute.isEmpty());
//
//        // 验证是否选择了总时间最短的路径（直达路线）
//        assertEquals(1, optimalRoute.size(), "应该选择直达路线");
//        Edge route = optimalRoute.get(0);
//        assertEquals(8, route.calculateDuration() / 60, "直达路线应该是8小时");
//    }
//
//    @Test
//    void testFindOptimalRouteWithSameTimeDifferentCost() {
//        // 准备测试数据：相同时间不同费用的路径
//        prepareTestRoute("西安", "上海", "K125", "10:00", "18:00", 300.0);  // 路线1
//        prepareTestRoute("西安", "上海", "K126", "10:00", "18:00", 200.0);  // 路线2
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "cost");
//        assertNotNull(optimalRoute);
//        assertFalse(optimalRoute.isEmpty());
//
//        // 验证是否选择了费用较低的路线
//        Edge route = optimalRoute.get(0);
//        assertEquals("K126", route.getRouteNo(), "应该选择费用较低的K126");
//        assertEquals(200.0, route.getFare(), "应该选择费用为200的路线");
//    }
//
//    @Test
//    void testFindOptimalRouteWithInvalidCriterion() {
//        // 测试无效的决策标准
//        assertThrows(IllegalArgumentException.class, () -> {
//            routeQueryService.findOptimalRoute("西安", "上海", "invalid");
//        });
//    }
//
//    @Test
//    void testFindOptimalRouteWithInvalidCity() {
//        // 测试不存在的城市
//        assertThrows(IllegalArgumentException.class, () -> {
//            routeQueryService.findOptimalRoute("西安", "不存在的城市", "cost");
//        });
//    }
//
//    @Test
//    void testFindDirectRoutesWithFilters() {
//        // 准备测试数据：多条直达路线
//        prepareTestRoute("西安", "北京", "K123", "08:00", "15:00", 100.0);
//        prepareTestRoute("西安", "北京", "G123", "09:00", "14:00", 200.0);
//        prepareTestRoute("西安", "北京", "D123", "10:00", "16:00", 150.0);
//
//        List<Edge> directRoutes = routeQueryService.findDirectRoutes("西安", "北京");
//        assertFalse(directRoutes.isEmpty());
//        assertEquals(3, directRoutes.size(), "应该找到3条直达路线");
//
//        // 验证路线是否按时间排序
//        assertTrue(directRoutes.stream()
//                .map(Edge::getDeparture)
//                .reduce((a, b) -> {
//                    assertTrue(a.compareTo(b) <= 0, "路线应该按发车时间排序");
//                    return b;
//                })
//                .isPresent());
//    }
//
//    @Test
//    void testRouteResponseFormat() {
//        // 准备测试数据
//        prepareTestRoute("西安", "上海", "K125", "10:00", "18:00", 300.0);
//
//        List<Edge> routes = routeQueryService.findDirectRoutes("西安", "上海");
//        assertFalse(routes.isEmpty(), "应该找到路线");
//
//        Edge route = routes.get(0);
//        // 验证基本字段不为空
//        assertNotNull(route.getFromCity(), "出发城市不应为空");
//        assertNotNull(route.getToCity(), "到达城市不应为空");
//        assertNotNull(route.getRouteNo(), "路线编号不应为空");
//        assertNotNull(route.getType(), "交通类型不应为空");
//
//        // 验证时间格式 (HH:mm)
//        assertTrue(route.getDeparture().matches("^([01]\\d|2[0-3]):([0-5]\\d)$"),
//                "出发时间格式应为HH:mm");
//        assertTrue(route.getArrival().matches("^([01]\\d|2[0-3]):([0-5]\\d)$"),
//                "到达时间格式应为HH:mm");
//
//        // 验证费用格式
//        assertTrue(route.getFare() > 0, "费用应该为正数");
//        double roundedFare = Math.round(route.getFare() * 10.0) / 10.0;
//        assertEquals(route.getFare(), roundedFare, 0.001, "费用应该最多保留一位小数");
//    }
//
//    @Test
//    void testOptimalRouteResponseFormat() {
//        // 准备测试数据
//        prepareTestRoute("西安", "北京", "K123", "08:00", "15:00", 100.0);
//        prepareTestRoute("北京", "上海", "K124", "16:00", "21:00", 100.0);
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "cost");
//        assertFalse(optimalRoute.isEmpty(), "应该找到最优路线");
//
//        // 验证路线连续性
//        for (int i = 1; i < optimalRoute.size(); i++) {
//            Edge prev = optimalRoute.get(i-1);
//            Edge curr = optimalRoute.get(i);
//            assertEquals(prev.getToCity(), curr.getFromCity(), "路线应该连续");
//            assertTrue(prev.getArrival().compareTo(curr.getDeparture()) <= 0,
//                    "到达时间应早于下一段出发时间");
//        }
//
//        // 验证总费用计算
//        double totalFare = optimalRoute.stream()
//                .mapToDouble(Edge::getFare)
//                .sum();
//        assertTrue(totalFare > 0, "总费用应该为正数");
//        double roundedTotalFare = Math.round(totalFare * 10.0) / 10.0;
//        assertEquals(totalFare, roundedTotalFare, 0.001, "总费用应该最多保留一位小数");
//    }
//
//    @Test
//    void testEmptyRouteResponse() {
//        // 测试不存在的路线（使用存在的城市，但没有连接）
//        List<Edge> routes = routeQueryService.findDirectRoutes("西安", "广州");
//        assertNotNull(routes, "返回值不应为null");
//        assertTrue(routes.isEmpty(), "不存在的路线应返回空列表");
//
//        List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "广州", "cost");
//        assertNotNull(optimalRoute, "返回值不应为null");
//        assertTrue(optimalRoute.isEmpty(), "找不到路径时应返回空列表");
//    }
//
//    @Test
//    void testInvalidCityResponse() {
//        // 测试不存在的城市
//        assertThrows(IllegalArgumentException.class, () -> {
//            routeQueryService.findDirectRoutes("西安", "不存在的城市");
//        }, "对不存在的城市应该抛出异常");
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            routeQueryService.findOptimalRoute("西安", "不存在的城市", "cost");
//        }, "对不存在的城市应该抛出异常");
//    }
//}