//package com.yueqi.ntas;
//
//import com.yueqi.ntas.exception.BusinessException;
//import com.yueqi.ntas.service.CityService;
//import com.yueqi.ntas.service.RouteManageService;
//import com.yueqi.ntas.service.GraphService;
//import com.yueqi.ntas.domain.request.RouteRequest;
//import com.yueqi.ntas.domain.entity.Edge;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class CityServiceIntegrationTest {
//
//    @Autowired
//    private CityService cityService;
//
//    @Autowired
//    private RouteManageService routeManageService;
//
//    @Autowired
//    private GraphService graphService;
//
//    @Test
//    void testAddCityAndVerifyFile() {
//        String cityName = "西安";
//
//        // 确保测试前城市不存在
//        List<String> citiesBefore = cityService.getAllCities();
//        assertFalse(citiesBefore.contains(cityName), "测试前城市不应该存在");
//
//        // 添加城市
//        cityService.addCity(cityName);
//
//        // 从数据库验证
//        List<String> citiesAfter = cityService.getAllCities();
//        assertTrue(citiesAfter.contains(cityName), "城市应该被添加到列表中");
//    }
//
//    @Test
//    void testAddExistingCity() {
//        String cityName = "重庆";
//        assertThrows(BusinessException.class, () -> {
//            cityService.addCity(cityName);
//        }, "添加已存在的城市应该抛出异常");
//    }
//
//    @Test
//    void testDeleteCityAndVerifyFile() {
//        // 准备测试数据
//        String cityToDelete = "西安";
//        String connectedCity = "北京";
//
//        // 执行删除操作
//        cityService.deleteCity(cityToDelete);
//
//        // 验证城市被删除
//        List<String> citiesAfter = cityService.getAllCities();
//        assertFalse(citiesAfter.contains(cityToDelete), "城市应该从列表中删除");
//
//        // 验证相关路线被删除
//        List<Edge> allRoutes = graphService.getAllEdges();
//        assertTrue(allRoutes.stream()
//                .noneMatch(route -> route.getFromCity().equals(cityToDelete)
//                        || route.getToCity().equals(cityToDelete)),
//                "与删除城市相关的所有路线都应该被删除");
//
//        // 验证其他城市仍然存在
//        assertTrue(citiesAfter.contains(connectedCity), "不相关的城市应该保持不变");
//    }
//}