package com.yueqi.ntas.controller;

import com.yueqi.ntas.domain.response.Result;
import com.yueqi.ntas.domain.response.AdjacentStationsResponse;
import com.yueqi.ntas.domain.validation.ValidateCityResponse;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.service.CityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/traffic/city")
public class CityController {
    @Autowired
    private CityService cityService;

    @GetMapping
    public Result<List<String>> getCities(@RequestParam(required = false) String query) {
        List<String> cities = cityService.getCities(query);
        return Result.success(cities);
    }

    @PostMapping
    public Result<Void> addCity(@RequestParam String cityName) {
        try {
            cityService.addCity(cityName);
            return Result.success(null);
        } catch (Exception e) {
            log.error("添加城市失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result<Void> deleteCity(@RequestParam String cityName) {
        try {
            log.info("开始删除城市: {}", cityName);
            cityService.deleteCity(cityName);
            log.info("城市删除成功: {}", cityName);
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除城市失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/validate")
    public Result<ValidateCityResponse> validateCity(@RequestParam String cityName) {
        ValidateCityResponse response = new ValidateCityResponse();
        List<String> errors = new ArrayList<>();

        if (cityName == null || cityName.trim().isEmpty()) {
            errors.add("城市名称不能为空");
        } else {
            cityName = cityName.trim();
            if (!cityName.matches("^[\\u4e00-\\u9fa5]+$")) {
                errors.add("城市名称只能包含中文字符");
            }
            if (cityService.exists(cityName)) {
                errors.add("城市已存在");
            }
        }

        response.setValid(errors.isEmpty());
        response.setErrors(errors);
        response.setFormattedName(cityName);

        return Result.success(response);
    }

    @GetMapping("/adjacent")
    public Result<AdjacentStationsResponse> getAdjacentStations(@RequestParam String fromCity) {
        try {
            List<String> adjacentCities = cityService.findAdjacentCities(fromCity);
            AdjacentStationsResponse response = new AdjacentStationsResponse();
            response.setAdjacentStations(adjacentCities);
            response.setMessage(String.format("找到 %d 个相邻站点", adjacentCities.size()));
            return Result.success(response);
        } catch (BusinessException e) {
            log.error("获取相邻站点失败", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("获取相邻站点失败", e);
            return Result.error(e.getMessage());
        }
    }
} 