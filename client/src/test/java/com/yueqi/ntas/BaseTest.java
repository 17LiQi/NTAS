package com.yueqi.ntas;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Map;

@Slf4j
@SpringBootTest
public abstract class BaseTest {
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @BeforeEach
    protected void setUp() {
        cleanAndInitDatabase();
    }

    private void cleanAndInitDatabase() {
        try {
            // 禁用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 清空表
            jdbcTemplate.execute("TRUNCATE TABLE route");
            jdbcTemplate.execute("TRUNCATE TABLE city");
            
            // 启用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            
            // 初始化基础数据 - 使用自增ID
            jdbcTemplate.execute(
                "INSERT INTO city (name) VALUES " +
                "('西安'),('北京'),('上海'),('广州')"
            );
            
            // 获取城市ID
            Integer xianId = jdbcTemplate.queryForObject(
                "SELECT id FROM city WHERE name = '西安'", Integer.class);
            Integer beijingId = jdbcTemplate.queryForObject(
                "SELECT id FROM city WHERE name = '北京'", Integer.class);
            Integer shanghaiId = jdbcTemplate.queryForObject(
                "SELECT id FROM city WHERE name = '上海'", Integer.class);
            
            log.info("城市ID: 西安={}, 北京={}, 上海={}", xianId, beijingId, shanghaiId);
            
            // 初始化路线数据 - 使用查询到的ID
            String routeSql = "INSERT INTO route (from_city_id, to_city_id, transport_type, route_no, departure, arrival, fare) " +
                "VALUES " +
                String.format("(%d, %d, '火车', 'K123', '08:00', '20:00', 100.0),", xianId, beijingId) +
                String.format("(%d, %d, '火车', 'K125', '10:00', '18:00', 300.0)", xianId, shanghaiId);
            
            log.info("执行路线SQL: {}", routeSql);
            jdbcTemplate.execute(routeSql);
            
            // 验证数据
            List<Map<String, Object>> routes = jdbcTemplate.queryForList("SELECT * FROM route");
            log.info("插入的路线数据: {}", routes);
            
        } catch (Exception e) {
            log.error("初始化测试数据失败", e);
            throw e;
        }
    }
} 