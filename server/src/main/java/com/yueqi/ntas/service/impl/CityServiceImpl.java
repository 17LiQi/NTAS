package com.yueqi.ntas.service.impl;

import com.yueqi.ntas.entity.City;
import com.yueqi.ntas.entity.Route;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.mapper.CityMapper;
import com.yueqi.ntas.mapper.RouteMapper;
import com.yueqi.ntas.service.CityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CityServiceImpl implements CityService {
    
    @Autowired
    private CityMapper cityMapper;
    
    @Autowired
    private RouteMapper routeMapper;

    @Override
    @Transactional
    public void addCity(String cityName) {
        // 先检查是否存在
        if (cityMapper.findByName(cityName) != null) {
            throw new BusinessException("城市已存在: " + cityName);
        }

        City city = new City();
        city.setName(cityName);
        cityMapper.insert(city);
    }

    @Override
    @Transactional
    public void deleteCity(String cityName) {
        try {
            log.info("开始删除城市: {}", cityName);
            City city = cityMapper.findByName(cityName);
            if (city == null) {
                throw new BusinessException("城市不存在");
            }

            // 删除以该城市为起点的路线
            List<Route> fromRoutes = routeMapper.findByFromCity(city.getId());
            for (Route route : fromRoutes) {
                routeMapper.delete(route);
                log.info("删除出发路线: {} -> {}", cityName, route.getToCityId());
            }

            // 删除以该城市为终点的路线
            List<Route> toRoutes = routeMapper.findByToCity(city.getId());
            for (Route route : toRoutes) {
                routeMapper.delete(route);
                log.info("删除到达路线: {} -> {}", route.getFromCityId(), cityName);
            }

            // 删除城市
            cityMapper.deleteByName(cityName);
            log.info("城市删除成功: {}", cityName);
        } catch (Exception e) {
            log.error("删除城市失败: {}", cityName, e);
            throw new BusinessException("删除城市失败: " + e.getMessage());
        }
    }

    @Override
    public boolean exists(String cityName) {
        try {
            return cityMapper.findByName(cityName) != null;
        } catch (Exception e) {
            log.error("检查城市存在性失败: {}", cityName, e);
            throw new BusinessException("检查城市失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getAllCities() {
        try {
            return cityMapper.findAll().stream()
                    .map(City::getName)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取所有城市失败", e);
            throw new BusinessException("获取城市列表失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> findAdjacentCities(String fromCity) {
        try {
            log.info("开始查找相邻城市: {}", fromCity);
            City city = cityMapper.findByName(fromCity);
            if (city == null) {
                throw new BusinessException("城市不存在");
            }

            // 从日志看，这里获取到了2条路线记录
            List<Route> routes = routeMapper.findByFromCity(city.getId());
            
            // 但是这里 route.getToCityId() 可能返回 null
            return routes.stream()
                    .map(route -> {
                        // 添加空值检查
                        Integer toCityId = route.getToCityId();
                        if (toCityId == null) {
                            log.warn("路线目的地ID为空: {}", route);
                            return null;
                        }
                        City toCity = cityMapper.findById(toCityId);
                        if (toCity == null) {
                            log.warn("找不到目的地城市: id={}", toCityId);
                            return null;
                        }
                        return toCity.getName();
                    })
                    .filter(name -> name != null)  // 过滤掉无效记录
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("查找相邻城市失败: {}", fromCity, e);
            throw new BusinessException("查找相邻城市失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> getCities(String query) {
        if (query == null || query.isEmpty()) {
            return cityMapper.findAll().stream()
                    .map(City::getName)
                    .sorted()
                    .collect(Collectors.toList());
        }
        
        return cityMapper.findByNameLike(query).stream()
                .map(City::getName)
                .collect(Collectors.toList());
    }
} 