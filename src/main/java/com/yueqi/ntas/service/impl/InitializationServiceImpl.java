package com.yueqi.ntas.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.yueqi.ntas.service.InitializationService;
import com.yueqi.ntas.mapper.RouteMapper;
import com.yueqi.ntas.mapper.CityMapper;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import javax.sql.DataSource;

@Service
public class InitializationServiceImpl implements InitializationService {
    private static final Logger log = LoggerFactory.getLogger(InitializationServiceImpl.class);
    
    @Autowired
    private RouteMapper routeMapper;
    
    @Autowired
    private CityMapper cityMapper;
    
    @Autowired
    private DataSource dataSource;
    
    @Value("${app.database.auto-init:false}")
    private boolean autoInit;
    
    @PostConstruct
    public void init() {
        if (autoInit) {
            initializeDatabase();
        } else {
            log.info("数据库自动初始化已禁用");
        }
    }
    
    @Override
    public void initializeDatabase() {
        if (!autoInit) {
            log.info("数据库自动初始化已禁用，跳过初始化");
            return;
        }
        
        if (!cityMapper.findAll().isEmpty()) {
            log.info("数据库已包含数据，跳过初始化");
            return;
        }
        
        log.info("开始初始化数据库...");
        try {
            Resource resource = new ClassPathResource("db/init.sql");
            ScriptUtils.executeSqlScript(
                dataSource.getConnection(), 
                resource
            );
            log.info("数据库初始化完成");
        } catch (Exception e) {
            log.error("数据库初始化失败", e);
            // 不抛出异常，只记录日志
            log.warn("继续使用现有数据库数据");
        }
    }
} 