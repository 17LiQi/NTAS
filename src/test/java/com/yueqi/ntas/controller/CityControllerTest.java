package com.yueqi.ntas.controller;

import com.yueqi.ntas.BaseTest;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import lombok.extern.slf4j.Slf4j;
import com.yueqi.ntas.service.CityService;

import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@Slf4j
class CityControllerTest extends BaseTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CityService cityService;

    @Autowired
    private CityMapper cityMapper;
    
    @Autowired
    private RouteMapper routeMapper;

    private static final String TEST_CITY = "测试城市";

    @BeforeEach
    protected void setUp() {
        // 确保测试城市不存在
        try {
            City city = cityMapper.findByName(TEST_CITY);
            if (city != null) {
                // 先删除相关路线
                List<Route> routes = routeMapper.findByFromCity(city.getId());
                for (Route route : routes) {
                    routeMapper.delete(route);
                }
                // 再删除城市
                cityMapper.deleteByName(TEST_CITY);
            }
        } catch (Exception ignored) {}
    }

    @Test
    void testGetCitiesWithQuery() throws Exception {
        mockMvc.perform(get("/traffic/city")
                .param("query", "西")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0]").value("西安"));
    }

    @Test
    void testValidateCity() throws Exception {
        mockMvc.perform(get("/traffic/city/validate")
                        .param("cityName", "test123")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.errors").isArray())
                .andExpect(jsonPath("$.data.errors[0]").value("城市名称只能包含中文字符"));
    }

    @Test
    void testGetAdjacentStations() throws Exception {
        mockMvc.perform(get("/traffic/city/adjacent")
                        .param("fromCity", "西安")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.adjacentStations").isArray())
                .andExpect(jsonPath("$.data.message").isString());
    }

    @Test
    void testAddCity() throws Exception {
        // 确保城市不存在
        assertFalse(cityService.exists(TEST_CITY), "城市不应该存在");

        mockMvc.perform(post("/traffic/city")
                .param("cityName", TEST_CITY)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void testDeleteCity() throws Exception {
        // 先添加测试城市
        if (!cityService.exists(TEST_CITY)) {
            cityService.addCity(TEST_CITY);
        }
        assertTrue(cityService.exists(TEST_CITY), "城市应该存在");

        // 删除城市
        mockMvc.perform(post("/traffic/city/delete")
                .param("cityName", TEST_CITY)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 验证城市已删除
        assertFalse(cityService.exists(TEST_CITY), "城市应该已被删除");
    }

    @Test
    void testGetCitiesWithEmptyQuery() throws Exception {
        mockMvc.perform(get("/traffic/city")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void testValidateInvalidCity() throws Exception {
        mockMvc.perform(get("/traffic/city/validate")
                        .param("cityName", "")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.valid").value(false))
                .andExpect(jsonPath("$.data.errors[*]").value(org.hamcrest.Matchers.hasItem("城市名称不能为空")));
    }

    @Test
    void testGetCitiesWithInvalidQuery() throws Exception {
        mockMvc.perform(get("/traffic/city")
                .param("query", "!@#$")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void testGetAdjacentStationsWithInvalidCity() throws Exception {
        mockMvc.perform(get("/traffic/city/adjacent")
                .param("fromCity", "不存在的城市")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
} 