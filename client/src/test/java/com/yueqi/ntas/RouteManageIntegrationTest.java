// package com.yueqi.ntas;

// import com.yueqi.ntas.domain.entity.Edge;
// import com.yueqi.ntas.domain.request.RouteRequest;
// import com.yueqi.ntas.exception.BusinessException;
// import com.yueqi.ntas.service.*;
// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// @SpringBootTest
// class RouteManageIntegrationTest {

//     private static final Logger log = LoggerFactory.getLogger(RouteManageIntegrationTest.class);

//     @Autowired
//     private RouteManageService routeManageService;

//     @Autowired
//     private GraphService graphService;

//     @Autowired
//     private DataService dataService;

//     @Autowired
//     private RouteQueryService routeQueryService;

//     private RouteRequest createTestRoute(String from, String to, String routeNo) {
//         RouteRequest request = new RouteRequest();
//         request.setFromCity(from);
//         request.setToCity(to);
//         request.setType("火车");
//         request.setRouteNo(routeNo);
//         request.setDeparture("08:00");
//         request.setArrival("20:00");
//         request.setFare(100.0);
//         return request;
//     }

//     private void prepareTestRoute(String from, String to, String routeNo, 
//             String departure, String arrival, double fare) {
//         // 检查路线是否已存在
//         List<Edge> existingRoutes = graphService.getDirectEdges(from, to);
//         boolean routeExists = existingRoutes.stream()
//                 .anyMatch(route -> route.getRouteNo().equals(routeNo));
        
//         if (!routeExists) {
//             RouteRequest request = new RouteRequest();
//             request.setFromCity(from);
//             request.setToCity(to);
//             request.setType("火车");
//             request.setRouteNo(routeNo);
//             request.setDeparture(departure);
//             request.setArrival(arrival);
//             request.setFare(fare);
//             routeManageService.addRoute(request);
//             log.info("添加测试路线: {} -> {}, 路线号: {}", from, to, routeNo);
//         } else {
//             log.info("测试路线已存在: {} -> {}, 路线号: {}", from, to, routeNo);
//         }
//     }

//     @Test
//     void testAddRoute() {
//         String from = "西安";
//         String to = "北京";
//         String routeNo = "K123";
        
//         RouteRequest request = createTestRoute(from, to, routeNo);
//         routeManageService.addRoute(request);
        
//         // 验证路线是否添加成功
//         List<Edge> routes = graphService.getDirectEdges(from, to);
//         assertFalse(routes.isEmpty(), "应该找到新添加的路线");
//         Edge addedRoute = routes.get(0);
//         assertEquals(routeNo, addedRoute.getRouteNo(), "路线编号应该匹配");
//     }

//     @Test
//     void testAddDuplicateRoute() {
//         String from = "西安";
//         String to = "北京";
//         String routeNo = "K123";
        
//         RouteRequest request = createTestRoute(from, to, routeNo);
        
//         // 尝试添加相同的路线
//         assertThrows(BusinessException.class, () -> {
//             routeManageService.addRoute(request);
//         }, "添加重复路线应该抛出异常");
//     }

//     @Test
//     void testDeleteRoute() {
//         String from = "西安";
//         String to = "北京";
//         String routeNo = "K123";
        
//         // 确保路线存在
//         prepareTestRoute(from, to, routeNo, "08:00", "20:00", 100.0);
        
//         // 删除路线
//         routeManageService.deleteRoute(from, to, routeNo, "08:00");
        
//         // 验证路线是否被删除
//         List<Edge> routes = graphService.getDirectEdges(from, to);
//         assertTrue(routes.isEmpty(), "路线应该被删除");
//     }

//     @Test
//     void testDeleteNonExistentRoute() {
//         assertThrows(BusinessException.class, () -> {
//             routeManageService.deleteRoute("西安", "北京", "K999", "08:00");
//         }, "删除不存在的路线应该抛出异常");
//     }

//     @Test
//     void testAddRouteWithInvalidTime() {
//         RouteRequest request = createTestRoute("西安", "北京", "K123");
//         request.setDeparture("25:00");  // 无效时间
        
//         assertThrows(BusinessException.class, () -> {
//             routeManageService.addRoute(request);
//         }, "添加无效时间的路线应该抛出异常");
//     }

//     @Test
//     void testAddRouteWithInvalidFare() {
//         RouteRequest request = createTestRoute("西安", "北京", "K123");
//         request.setFare(-100.0);  // 无效票价
        
//         assertThrows(BusinessException.class, () -> {
//             routeManageService.addRoute(request);
//         }, "添加无效票价的路线应该抛出异常");
//     }

//     @Test
//     void testUpdateRoute() {
//         String from = "西安";
//         String to = "北京";
//         String routeNo = "K123";
        
//         // 先添加路线
//         RouteRequest original = createTestRoute(from, to, routeNo);
//         routeManageService.addRoute(original);
        
//         // 修改路线
//         routeManageService.deleteRoute(from, to, routeNo, "08:00");
//         RouteRequest updated = createTestRoute(from, to, routeNo);
//         updated.setFare(150.0);
//         routeManageService.addRoute(updated);
        
//         // 验证修改
//         List<Edge> routes = graphService.getDirectEdges(from, to);
//         assertEquals(150.0, routes.get(0).getFare(), "票价应该被更新");
//     }

//     @Test
//     void testFindOptimalRouteWithNoPath() {
//         // 测试不存在路径的情况
//         List<Edge> route = routeQueryService.findOptimalRoute("西安", "广州", "cost");
//         assertTrue(route.isEmpty(), "不存在的路径应该返回空列表");
//     }

//     @Test
//     void testFindOptimalRouteWithMultiplePaths() {
//         // 准备测试数据：多条可选路径
//         prepareTestRoute("西安", "上海", "K125", "10:00", "22:00", 300.0);  // 直达
//         prepareTestRoute("西安", "郑州", "K126", "08:00", "12:00", 80.0);   // 路径1-1
//         prepareTestRoute("郑州", "上海", "K127", "13:00", "18:00", 100.0);  // 路径1-2
//         prepareTestRoute("西安", "北京", "K128", "09:00", "15:00", 90.0);   // 路径2-1
//         prepareTestRoute("北京", "上海", "K129", "16:00", "20:00", 120.0);  // 路径2-2

//         List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "cost");
//         assertNotNull(optimalRoute);
//         assertFalse(optimalRoute.isEmpty());

//         // 验证是否选择了总费用最低的路径（西安-郑州-上海）
//         double totalFare = optimalRoute.stream()
//                 .mapToDouble(Edge::getFare)
//                 .sum();
//         assertEquals(180.0, totalFare, "应该选择总费用为180的路线");
//         assertEquals(2, optimalRoute.size(), "应该是两段路线");
//     }

//     @Test
//     void testFindOptimalRouteWithTimeConstraint() {
//         // 准备测试数据：考虑时间约束的路径
//         prepareTestRoute("西安", "上海", "K125", "10:00", "18:00", 300.0);  // 直达8小时
//         prepareTestRoute("西安", "郑州", "K126", "08:00", "12:00", 80.0);   // 4小时
//         prepareTestRoute("郑州", "上海", "K127", "13:00", "18:00", 100.0);  // 5小时+1小时等待

//         List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "time");
//         assertNotNull(optimalRoute);
//         assertFalse(optimalRoute.isEmpty());

//         // 验证是否选择了总时间最短的路径（直达路线）
//         assertEquals(1, optimalRoute.size(), "应该选择直达路线");
//         Edge route = optimalRoute.get(0);
//         assertEquals(8, route.calculateDuration() / 60, "直达路线应该是8小时");
//     }

//     @Test
//     void testFindOptimalRouteWithSameTimeDifferentCost() {
//         // 清理所有现有路线
//         clearAllRoutes();
        
//         // 准备测试数据：相同时间不同费用的路径
//         // 只添加两条直达路线用于测试
//         RouteRequest route1 = new RouteRequest();
//         route1.setFromCity("西安");
//         route1.setToCity("上海");
//         route1.setType("火车");
//         route1.setRouteNo("K125");
//         route1.setDeparture("10:00");
//         route1.setArrival("18:00");
//         route1.setFare(300.0);
//         routeManageService.addRoute(route1);
        
//         RouteRequest route2 = new RouteRequest();
//         route2.setFromCity("西安");
//         route2.setToCity("上海");
//         route2.setType("火车");
//         route2.setRouteNo("K126");
//         route2.setDeparture("10:00");
//         route2.setArrival("18:00");
//         route2.setFare(200.0);
//         routeManageService.addRoute(route2);
        
//         List<Edge> optimalRoute = routeQueryService.findOptimalRoute("西安", "上海", "cost");
//         assertNotNull(optimalRoute);
//         assertFalse(optimalRoute.isEmpty());
        
//         // 验证是否选择了费用较低的路线
//         Edge route = optimalRoute.get(0);
//         assertEquals("K126", route.getRouteNo(), "应该选择费用较低的K126");
//         assertEquals(200.0, route.getFare(), "应该选择费用为200的路线");
//     }

//     @Test
//     void testFindOptimalRouteWithInvalidCriterion() {
//         // 测试无效的决策标准
//         assertThrows(IllegalArgumentException.class, () -> {
//             routeQueryService.findOptimalRoute("西安", "上海", "invalid");
//         });
//     }

//     @Test
//     void testFindOptimalRouteWithInvalidCity() {
//         // 测试不存在的城市
//         assertThrows(IllegalArgumentException.class, () -> {
//             routeQueryService.findOptimalRoute("西安", "不存在的城市", "cost");
//         });
//     }

//     @Test
//     void testFindDirectRoutesWithFilters() {
//         // 准备测试数据：多条直达路线
//         prepareTestRoute("西安", "北京", "K123", "08:00", "15:00", 100.0);
//         prepareTestRoute("西安", "北京", "G123", "09:00", "14:00", 200.0);
//         prepareTestRoute("西安", "北京", "D123", "10:00", "16:00", 150.0);

//         List<Edge> directRoutes = routeQueryService.findDirectRoutes("西安", "北京");
//         assertFalse(directRoutes.isEmpty());
//         assertEquals(3, directRoutes.size(), "应该找到3条直达路线");

//         // 验证路线是否按时间排序
//         assertTrue(directRoutes.stream()
//                 .map(Edge::getDeparture)
//                 .reduce((a, b) -> {
//                     assertTrue(a.compareTo(b) <= 0, "路线应该按发车时间排序");
//                     return b;
//                 })
//                 .isPresent());
//     }

//     private void clearAllRoutes() {
//         List<Edge> allRoutes = graphService.getAllEdges();
//         for (Edge route : allRoutes) {
//             routeManageService.deleteRoute(
//                 route.getFromCity(),
//                 route.getToCity(),
//                 route.getRouteNo(),
//                 route.getDeparture()
//             );
//         }
//     }
// } 