package com.yueqi.ntas.service.impl;

import com.yueqi.ntas.domain.entity.Edge;
import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import com.yueqi.ntas.service.RouteManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
public class RouteManageServiceImpl implements RouteManageService {
    
    @Autowired
    private RouteMapper routeMapper;
    
    @Autowired
    private CityMapper cityMapper;

    @Override
    @Transactional
    public void addRoute(RouteRequest request) {
        try {
            log.info("开始添加路线: {} -> {}", request.getFromCity(), request.getToCity());
            
            // 验证时间格式
            validateTime(request.getDeparture(), "出发时间");
            validateTime(request.getArrival(), "到达时间");
            
            // 验证票价
            if (request.getFare() <= 0) {
                throw new BusinessException("票价必须大于0");
            }
            
            // 先检查城市是否存在
            City fromCity = cityMapper.findByName(request.getFromCity());
            City toCity = cityMapper.findByName(request.getToCity());
            if (fromCity == null || toCity == null) {
                throw new BusinessException("城市不存在");
            }
            
            // 检查路线是否已存在
            if (routeMapper.checkExists(fromCity.getId(), toCity.getId(), 
                    request.getRouteNo(), request.getDeparture()) > 0) {
                throw new BusinessException("路线已存在");
            }
            
            // 插入路线
            Route route = new Route();
            route.setFromCityId(fromCity.getId());
            route.setToCityId(toCity.getId());
            route.setTransportType(request.getType());
            route.setRouteNo(request.getRouteNo());
            route.setDeparture(LocalTime.parse(request.getDeparture()));
            route.setArrival(LocalTime.parse(request.getArrival()));
            route.setFare(BigDecimal.valueOf(request.getFare()));
            
            routeMapper.insert(route);
            log.info("路线添加成功");
        } catch (Exception e) {
            log.error("添加路线失败", e);
            throw e instanceof BusinessException ? (BusinessException)e : 
                new BusinessException(e.getMessage());
        }
    }

    private void validateTime(String time, String fieldName) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) {
                throw new BusinessException(fieldName + "格式不正确，应为HH:mm格式");
            }
            
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            
            if (hours < 0 || hours >= 24 || minutes < 0 || minutes >= 60) {
                throw new BusinessException(fieldName + "不是有效的时间");
            }
        } catch (NumberFormatException e) {
            throw new BusinessException(fieldName + "格式不正确，应为HH:mm格式");
        }
    }

    @Override
    @Transactional
    public void deleteRoute(String from, String to, String routeNo, String departure) {
        try {
            log.info("开始删除路线: {} -> {}, 路线号: {}", from, to, routeNo);
            
            City fromCity = cityMapper.findByName(from);
            City toCity = cityMapper.findByName(to);
            if (fromCity == null || toCity == null) {
                throw new BusinessException("城市不存在");
            }
            
            Route route = Route.builder()
                .fromCityId(fromCity.getId())
                .toCityId(toCity.getId())
                .routeNo(routeNo)
                .departure(LocalTime.parse(departure))
                .build();
                
            routeMapper.delete(route);
            log.info("路线删除成功");
        } catch (Exception e) {
            log.error("删除路线失败", e);
            throw e instanceof BusinessException ? (BusinessException)e : 
                new BusinessException(e.getMessage());
        }
    }

    @Override
    public boolean exists(String from, String to, String routeNo, String departure) {
        try {
            City fromCity = cityMapper.findByName(from);
            City toCity = cityMapper.findByName(to);
            if (fromCity == null || toCity == null) {
                return false;
            }
            
            List<Route> routes = routeMapper.findByFromAndToCity(fromCity.getId(), toCity.getId());
            return routes.stream()
                    .anyMatch(route -> route.getRouteNo().equals(routeNo) 
                            && route.getDeparture().toString().equals(departure));
        } catch (Exception e) {
            log.error("检查路线存在性失败", e);
            throw new BusinessException("检查路线失败: " + e.getMessage());
        }
    }
} 