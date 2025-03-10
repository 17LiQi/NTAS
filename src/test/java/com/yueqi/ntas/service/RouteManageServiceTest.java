//package com.yueqi.ntas.service;
//
//import com.yueqi.ntas.domain.request.RouteRequest;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//class RouteManageServiceTest {
//
//    @Autowired
//    private RouteManageService routeManageService;
//
//    @MockBean
//    private GraphService graphService;
//
//    @Test
//    void testAddRoute() {
//        RouteRequest request = new RouteRequest();
//        request.setFromCity("西安");
//        request.setToCity("北京");
//        request.setType("火车");
//        request.setRouteNo("K123");
//        request.setDeparture("08:00");
//        request.setArrival("20:00");
//        request.setFare(100.0);
//
//        routeManageService.addRoute(request);
//        verify(graphService).addEdge(request);
//    }
//
//    @Test
//    void testDeleteRoute() {
//        String from = "西安";
//        String to = "北京";
//        String routeNo = "K123";
//        String departure = "08:00";
//
//        routeManageService.deleteRoute(from, to, routeNo, departure);
//        verify(graphService).removeEdge(from, to, routeNo, departure);
//    }
//
//    @Test
//    void testExists() {
//        String from = "西安";
//        String to = "北京";
//        String routeNo = "K123";
//        String departure = "08:00";
//
//        when(routeManageService.exists(from, to, routeNo, departure)).thenReturn(true);
//        assertTrue(routeManageService.exists(from, to, routeNo, departure));
//    }
//}