package com.yueqi.ntas.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.response.OptimalRouteResponse;
import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 路线优化集成测试
 * 使用实际数据库测试最优路线查询功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("prod")
@Slf4j
public class RouteOptimizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private CityMapper cityMapper;

    @Test
    void testFindOptimalRouteByTime() throws Exception {
        // 测试从上海到北京的最短时间路线
        MvcResult result = mockMvc.perform(get("/traffic/route/query/optimal")
                .param("start", "北京")
                .param("end", "上海")
                .param("criterion", "time")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.totalFare").isNumber())
                .andExpect(jsonPath("$.data.totalTime").exists())
                .andExpect(jsonPath("$.data.transferCount").isNumber())
                .andExpect(jsonPath("$.data.totalWaitTime").exists())
                .andExpect(jsonPath("$.data.routeSummary").exists())
                .andReturn();

        // 打印响应内容
        String responseBody = result.getResponse().getContentAsString();
        log.info("最短时间路线查询响应: {}", responseBody);
        
        // 解析响应验证细节
        OptimalRouteResponse response = objectMapper.readValue(
            objectMapper.readTree(responseBody).get("data").toString(),
            OptimalRouteResponse.class
        );
        
        log.info("路线概要: {}", response.getRouteSummary());
        log.info("总费用: {}元", response.getTotalFare());
        log.info("总耗时: {}", response.getTotalTime());
        log.info("中转次数: {}", response.getTransferCount());
        log.info("等待时间: {}", response.getTotalWaitTime());
    }

    @Test
    void testFindOptimalRouteByCost() throws Exception {
        // 测试从上海到北京的最低费用路线
        MvcResult result = mockMvc.perform(get("/traffic/route/query/optimal")
                .param("start", "上海")
                .param("end", "北京")
                .param("criterion", "cost")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").exists())
                .andReturn();

        // 打印响应内容
        String responseBody = result.getResponse().getContentAsString();
        log.info("最低费用路线查询响应: {}", responseBody);
        
        // 其他验证逻辑保持不变...
    }

    @Test
    void testFindDirectRoutes() throws Exception {
        // 测试查询上海的所有相邻站点
        MvcResult result = mockMvc.perform(get("/traffic/route/query/direct")
                .param("from", "上海")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andReturn();

        // 打印响应内容
        String responseBody = result.getResponse().getContentAsString();
        log.info("上海相邻站点查询响应: {}", responseBody);

        // 验证数据
        List<Route> dbRoutes = routeMapper.findAll();
        log.info("数据库中共有 {} 条路线", dbRoutes.size());
        dbRoutes.forEach(route -> {
            if (cityMapper.findById(route.getFromCityId()).getName().equals("上海")) {
                log.info("直达路线: 上海 -> {}, {}{}, 发车:{}, 到达:{}, 票价:{}元",
                    cityMapper.findById(route.getToCityId()).getName(),
                    route.getTransportType(),
                    route.getRouteNo(),
                    route.getDeparture(),
                    route.getArrival(),
                    route.getFare()
                );
            }
        });
    }
} 