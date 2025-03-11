package com.yueqi.ntas.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yueqi.ntas.BaseTest;
import com.yueqi.ntas.domain.request.RouteRequest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 路线管理控制器测试类
 * 测试路线的增删改查和验证功能
 */
@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
class RouteManageControllerTest extends BaseTest {
   @Autowired
   private MockMvc mockMvc;
   @Autowired
   private ObjectMapper objectMapper;

   /**
    * 测试路线验证功能
    * 目的：验证当输入无效时间格式时，系统能正确识别并返回错误信息
    */
   @Test
   void testValidateRoute() throws Exception {
       RouteRequest request = new RouteRequest();
       request.setFromCity("西安");
       request.setToCity("北京");
       request.setType("火车");
       request.setRouteNo("K123");
       request.setDeparture("25:00"); // 无效时间
       request.setArrival("15:00");   // 添加必填字段
       request.setFare(100.0);        // 添加必填字段

       mockMvc.perform(post("/traffic/route/validate")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.valid").value(false))
               .andExpect(jsonPath("$.data.errors").isArray())
               .andExpect(jsonPath("$.data.errors[*]").value(org.hamcrest.Matchers.hasItem("时间格式不正确")));
   }

   /**
    * 测试获取删除确认信息
    * 目的：验证系统能正确返回路线描述和确认信息
    */
   @Test
   void testGetDeleteConfirm() throws Exception {
       mockMvc.perform(get("/traffic/route/delete/confirm")
               .param("fromCity", "西安")
               .param("toCity", "北京")
               .param("type", "火车")
               .param("routeNo", "K123")
               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.routeDescription").isString())
               .andExpect(jsonPath("$.data.confirmMessage").isString());
   }

   /**
    * 测试添加路线功能
    * 目的：验证系统能成功添加有效的路线信息
    */
   @Test
   void testAddRoute() throws Exception {
       RouteRequest request = new RouteRequest();
       request.setFromCity("西安");
       request.setToCity("北京");
       request.setType("火车");
       request.setRouteNo("K888");
       request.setDeparture("08:00");
       request.setArrival("15:00");
       request.setFare(100.0);

       mockMvc.perform(post("/traffic/route")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(200));
   }

   /**
    * 测试删除路线功能
    * 目的：验证系统能成功删除指定的路线
    */
   @Test
   void testDeleteRoute() throws Exception {
       mockMvc.perform(post("/traffic/route/delete")
               .contentType(MediaType.APPLICATION_JSON)
               .content("{\"fromCity\":\"西安\",\"toCity\":\"北京\",\"routeNo\":\"K123\",\"departure\":\"08:00\"}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(200));
   }

   /**
    * 测试无效路线验证
    * 目的：验证当出发地和目的地相同时，系统能正确识别并返回错误信息
    */
   @Test
   void testValidateInvalidRoute() throws Exception {
       RouteRequest request = new RouteRequest();
       request.setFromCity("西安");
       request.setToCity("西安"); // 出发地和目的地相同
       request.setType("火车");   // 添加必填字段
       request.setRouteNo("K888"); // 添加必填字段
       request.setDeparture("08:00"); // 添加必填字段
       request.setArrival("15:00");   // 添加必填字段
       request.setFare(100.0);        // 添加必填字段

       mockMvc.perform(post("/traffic/route/validate")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.valid").value(false))
               .andExpect(jsonPath("$.data.errors[*]").value(
                   org.hamcrest.Matchers.hasItem("出发城市和目的城市不能相同")));
   }

   /**
    * 测试无效时间格式验证
    * 目的：验证系统能正确识别并处理无效的时间格式
    */
   @Test
   void testValidateRouteWithInvalidTime() throws Exception {
       RouteRequest request = new RouteRequest();
       request.setFromCity("西安");
       request.setToCity("北京");
       request.setType("火车");   // 添加必填字段
       request.setRouteNo("K888"); // 添加必填字段
       request.setDeparture("25:00");  // 无效时间
       request.setArrival("26:00");    // 无效时间
       request.setFare(100.0);        // 添加必填字段

       mockMvc.perform(post("/traffic/route/validate")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.valid").value(false))
               .andExpect(jsonPath("$.data.errors[*]").value(
                   org.hamcrest.Matchers.hasItem("时间格式不正确")));
   }

   /**
    * 测试缺失字段验证
    * 目的：验证当请求缺少必要字段时，系统能正确处理
    */
   @Test
   void testValidateRouteWithMissingFields() throws Exception {
       RouteRequest request = new RouteRequest();  // 空请求

       mockMvc.perform(post("/traffic/route/validate")
               .contentType(MediaType.APPLICATION_JSON)
               .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.valid").value(false));
   }

   /**
    * 测试无效参数的删除确认
    * 目的：验证当删除确认请求包含无效参数时，系统的处理方式
    */
   @Test
   void testDeleteConfirmWithInvalidParams() throws Exception {
       mockMvc.perform(get("/traffic/route/delete/confirm")
               .param("fromCity", "西安")  // 提供有效值
               .param("toCity", "北京")    // 提供有效值
               .param("type", "火车")      // 提供有效值
               .param("routeNo", "K123")   // 提供有效值
               .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.data.routeDescription").isString())
               .andExpect(jsonPath("$.data.confirmMessage").isString());
   }
}