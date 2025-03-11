package com.yueqi.ntas.controller;

import com.yueqi.ntas.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 路线查询控制器测试类
 * 测试路线的各种查询功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class RouteQueryControllerTest extends BaseTest {
    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试获取所有路线
     * 目的：验证系统能正确返回所有路线，且价格格式正确
     */
    @Test
    void testGetAllRoutes() throws Exception {
        mockMvc.perform(get("/traffic/route/query/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].formattedFare").value(
                    org.hamcrest.Matchers.matchesPattern("\\d+\\.\\d 元")));
    }

    /**
     * 测试查询最优路线
     * 目的：验证系统能根据给定条件返回最优路线
     */
    @Test
    void testFindOptimalRouteWithDetails() throws Exception {
        mockMvc.perform(get("/traffic/route/query/optimal")
                .param("start", "西安")
                .param("end", "上海")  // 使用确定存在的路线
                .param("criterion", "cost")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.totalFare").isNumber())
                .andExpect(jsonPath("$.data.totalDuration").isString())
                .andExpect(jsonPath("$.data.routePath").isString());
    }

    /**
     * 测试查询直达路线
     * 目的：验证系统能正确返回两个城市间的直达路线
     */
    @Test
    void testFindDirectRoutesWithFilter() throws Exception {
        mockMvc.perform(get("/traffic/route/query/direct")
                .param("from", "西安")
                .param("to", "北京")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].routeNo").value("K123"));
    }

    /**
     * 测试无效优化条件
     * 目的：验证系统对无效的优化条件参数的处理
     */
    @Test
    void testFindOptimalRouteWithInvalidCriterion() throws Exception {
        mockMvc.perform(get("/traffic/route/query/optimal")
                .param("start", "西安")
                .param("end", "北京")
                .param("criterion", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 测试不存在的城市
     * 目的：验证系统对不存在城市的处理
     */
    @Test
    void testFindOptimalRouteWithNonexistentCity() throws Exception {
        mockMvc.perform(get("/traffic/route/query/optimal")
                .param("start", "不存在的城市")
                .param("end", "北京")
                .param("criterion", "cost")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 测试空参数查询
     * 目的：验证系统在无参数时返回所有路线
     */
    @Test
    void testFindDirectRoutesWithEmptyParams() throws Exception {
        mockMvc.perform(get("/traffic/route/query/direct")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray());
    }

    /**
     * 测试相同城市查询
     * 目的：验证系统对起点终点相同的处理
     */
    @Test
    void testFindOptimalRouteWithSameCity() throws Exception {
        mockMvc.perform(get("/traffic/route/query/optimal")
                .param("start", "西安")
                .param("end", "西安")
                .param("criterion", "cost")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    /**
     * 测试价格格式化
     * 目的：验证所有返回的价格格式是否正确
     */
    @Test
    void testGetAllRoutesFormatting() throws Exception {
        mockMvc.perform(get("/traffic/route/query/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[*].formattedFare").value(
                    org.hamcrest.Matchers.everyItem(
                        org.hamcrest.Matchers.matchesPattern("\\d+\\.\\d 元")
                    )
                ));
    }
}