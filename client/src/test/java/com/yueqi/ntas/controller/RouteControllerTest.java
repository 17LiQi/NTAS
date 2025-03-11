package com.yueqi.ntas.controller;

import com.yueqi.ntas.BaseTest;
import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.service.RouteManageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Slf4j
class RouteControllerTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RouteManageService routeManageService;

    private static final String TEST_ROUTE_NO = "K999";

    @BeforeEach
    protected void setUp() {
        // 调用父类的 setUp 初始化基础数据
        super.setUp();
        
        // 添加测试专用路线
        RouteRequest request = new RouteRequest();
        request.setFromCity("西安");
        request.setToCity("北京");
        request.setType("火车");
        request.setRouteNo(TEST_ROUTE_NO);
        request.setDeparture("08:00");
        request.setArrival("15:00");
        request.setFare(100.0);
        
        try {
            routeManageService.addRoute(request);
        } catch (Exception e) {
            log.warn("添加测试路线失败", e);
        }
    }

    @AfterEach
    void tearDown() {
        // 清理测试数据
        try {
            routeManageService.deleteRoute("西安", "北京", "K123", "08:00");
        } catch (Exception ignored) {}
    }

    @Test
    void testQueryDirectRoutes() throws Exception {
        mockMvc.perform(get("/traffic/route/direct")
                .param("from", "西安")
                .param("to", "北京")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("success"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fromCity").value("西安"))
                .andExpect(jsonPath("$.data[0].toCity").value("北京"))
                .andExpect(jsonPath("$.data[0].routeNo").value(TEST_ROUTE_NO));
    }


    @Test
    void testQueryOptimalRoute() throws Exception {
        // 先尝试删除可能存在的路线
        try {
            routeManageService.deleteRoute("西安", "上海", "K125", "10:00");
        } catch (Exception ignored) {}

        // 添加测试路线数据
        RouteRequest route1 = new RouteRequest();
        route1.setFromCity("西安");
        route1.setToCity("上海");
        route1.setType("火车");
        route1.setRouteNo("K125");
        route1.setDeparture("10:00");
        route1.setArrival("18:00");
        route1.setFare(300.0);
        try {
            routeManageService.addRoute(route1);
        } catch (BusinessException e) {
            // 如果路线已存在，可以继续测试
            log.info("路线已存在，继续测试");
        }

        MvcResult result = mockMvc.perform(get("/traffic/route/optimal")
                .param("start", "西安")
                .param("end", "上海")
                .param("criterion", "cost")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].fromCity").value("西安"))
                .andExpect(jsonPath("$.data[0].toCity").value("上海"))
                .andExpect(jsonPath("$.data[0].routeNo").value("K125"))
                .andExpect(jsonPath("$.data[0].fare").value(300.0))
                .andReturn();

        // 清理测试数据
        routeManageService.deleteRoute("西安", "上海", "K125", "10:00");
    }

    @Test
    void testInvalidCityParameter() throws Exception {
        mockMvc.perform(get("/traffic/route/direct")
                .param("from", "不存在的城市")
                .param("to", "北京")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").isString());
    }

    @Test
    void testInvalidCriterionParameter() throws Exception {
        mockMvc.perform(get("/traffic/route/optimal")
                .param("start", "西安")
                .param("end", "上海")
                .param("criterion", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").isString());
    }

    @Test
    void testMissingRequiredParameter() throws Exception {
        mockMvc.perform(get("/traffic/route/direct")
                .param("from", "西安")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.msg").value("Required parameter 'to' is not present"));
    }

    @Test
    void testNoRouteFound() throws Exception {
        mockMvc.perform(get("/traffic/route/direct")
                .param("from", "西安")
                .param("to", "广州")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testResponseHeaders() throws Exception {
        mockMvc.perform(get("/traffic/route/direct")
                .param("from", "西安")
                .param("to", "北京"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_JSON_VALUE))
                .andExpect(header().exists("Date"));
    }

    @Test
    void testCorsSupport() throws Exception {
        mockMvc.perform(options("/traffic/route/direct")
                .header("Access-Control-Request-Method", "GET")
                .header("Origin", "http://localhost:3000"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}