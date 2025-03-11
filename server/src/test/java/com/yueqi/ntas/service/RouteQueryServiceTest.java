//package com.yueqi.ntas.service;
//
//import com.yueqi.ntas.domain.entity.Edge;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//class RouteQueryServiceTest {
//
//    @Autowired
//    private RouteQueryService routeQueryService;
//
//    @MockBean
//    private GraphService graphService;
//
//    @Test
//    void testFindAllRoutes() {
//        List<Edge> expectedRoutes = Arrays.asList(
//            new Edge(1, "火车", "K123", "08:00", "20:00", 100.0, null),
//            new Edge(2, "飞机", "MU123", "10:00", "12:00", 800.0, null)
//        );
//        when(graphService.getAllEdges()).thenReturn(expectedRoutes);
//
//        List<Edge> actualRoutes = routeQueryService.findAllRoutes();
//        assertEquals(expectedRoutes, actualRoutes);
//    }
//
//    @Test
//    void testFindDirectRoutes() {
//        String from = "西安";
//        String to = "北京";
//        List<Edge> expectedRoutes = Arrays.asList(
//            new Edge(1, "火车", "K123", "08:00", "20:00", 100.0, null)
//        );
//        when(graphService.getDirectEdges(from, to)).thenReturn(expectedRoutes);
//
//        List<Edge> actualRoutes = routeQueryService.findDirectRoutes(from, to);
//        assertEquals(expectedRoutes, actualRoutes);
//    }
//
//    @Test
//    void testFindOptimalRoute() {
//        String start = "西安";
//        String end = "北京";
//        String criterion = "time";
//        List<Edge> expectedRoute = Arrays.asList(
//            new Edge(1, "火车", "K123", "08:00", "20:00", 100.0, null)
//        );
//        when(graphService.findShortestPath(start, end, criterion)).thenReturn(expectedRoute);
//
//        List<Edge> actualRoute = routeQueryService.findOptimalRoute(start, end, criterion);
//        assertEquals(expectedRoute, actualRoute);
//    }
//}