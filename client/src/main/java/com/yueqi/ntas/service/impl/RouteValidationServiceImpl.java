package com.yueqi.ntas.service.impl;

import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.domain.response.ValidationResponse;
import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import com.yueqi.ntas.service.RouteValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class RouteValidationServiceImpl implements RouteValidationService {
    @Autowired
    private RouteMapper routeMapper;
    
    @Autowired
    private CityMapper cityMapper;

    @Override
    public ValidationResponse validateRoute(RouteRequest request) {
        List<String> errors = new ArrayList<>();
        
        // 验证城市
        City fromCity = cityMapper.findByName(request.getFromCity());
        City toCity = cityMapper.findByName(request.getToCity());
        
        if (fromCity == null) {
            errors.add("出发城市不存在");
        }
        if (toCity == null) {
            errors.add("到达城市不存在");
        }
        
        // 验证时间格式
        try {
            LocalTime.parse(request.getDeparture());
            LocalTime.parse(request.getArrival());
        } catch (Exception e) {
            errors.add("时间格式不正确");
        }
        
        // 验证票价
        if (request.getFare() <= 0) {
            errors.add("票价必须大于0");
        }
        
        return new ValidationResponse(errors.isEmpty(), errors);
    }

    @Override
    public ValidationResponse validateRouteForDeletion(String from, String to, String routeNo, String departure) {
        List<String> errors = new ArrayList<>();
        
        City fromCity = cityMapper.findByName(from);
        City toCity = cityMapper.findByName(to);
        
        if (fromCity == null || toCity == null) {
            errors.add("城市不存在");
            return new ValidationResponse(false, errors);
        }
        
        Route route = Route.builder()
                .fromCityId(fromCity.getId())
                .toCityId(toCity.getId())
                .routeNo(routeNo)
                .departure(LocalTime.parse(departure))
                .build();
                
        if (routeMapper.findByFromAndToCity(fromCity.getId(), toCity.getId()).isEmpty()) {
            errors.add("路线不存在");
        }
        
        return new ValidationResponse(errors.isEmpty(), errors);
    }
} 