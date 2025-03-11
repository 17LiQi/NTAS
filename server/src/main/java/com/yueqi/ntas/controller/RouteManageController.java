package com.yueqi.ntas.controller;

import com.yueqi.ntas.domain.response.Result;
import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.domain.request.RouteDeleteRequest;
import com.yueqi.ntas.domain.response.RouteDeleteConfirmResponse;
import com.yueqi.ntas.domain.validation.ValidateRouteResponse;
import com.yueqi.ntas.exception.BusinessException;
import com.yueqi.ntas.service.RouteManageService;
import com.yueqi.ntas.service.RouteValidationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/traffic/route")
public class RouteManageController {
    @Autowired
    private RouteManageService routeManageService;

    @Autowired
    private RouteValidationService validationService;

    @PostMapping
    public Result<Void> addRoute(@RequestBody RouteRequest request) {
        try {
            routeManageService.addRoute(request);
            return Result.success(null);
        } catch (Exception e) {
            log.error("添加路线失败", e);
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/delete")
    public Result<Void> deleteRoute(@RequestBody RouteRequest request) {
        try {
            routeManageService.deleteRoute(
                request.getFromCity(),
                request.getToCity(),
                request.getRouteNo(),
                request.getDeparture()
            );
            return Result.success(null);
        } catch (Exception e) {
            log.error("删除路线失败", e);
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/delete/confirm")
    public Result<RouteDeleteConfirmResponse> getDeleteConfirm(
            @RequestParam String fromCity,
            @RequestParam String toCity,
            @RequestParam String type,
            @RequestParam(name = "flightNo", required = false) String flightNo,
            @RequestParam(name = "routeNo", required = false) String routeNo) {
        try {
            String actualRouteNo = routeNo != null ? routeNo : flightNo;
            if (actualRouteNo == null) {
                throw new BusinessException("路线编号不能为空");
            }
            
            RouteDeleteConfirmResponse response = new RouteDeleteConfirmResponse();
            response.setRouteDescription(
                String.format("从 %s 到 %s 的 %s%s 路线", fromCity, toCity, type, actualRouteNo)
            );
            response.setConfirmMessage("确定要删除该路线吗？");
            return Result.success(response);
        } catch (BusinessException e) {
            log.error("获取删除确认信息失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取删除确认信息失败", e);
            return Result.error("获取删除确认信息失败");
        }
    }

    @PostMapping("/validate")
    public Result<ValidateRouteResponse> validateRoute(@RequestBody RouteRequest request) {
        ValidateRouteResponse response = new ValidateRouteResponse();
        List<String> errors = new ArrayList<>();

        // 检查必填字段
        if (request.getFromCity() == null || request.getFromCity().trim().isEmpty()) {
            errors.add("出发城市不能为空");
        }
        if (request.getToCity() == null || request.getToCity().trim().isEmpty()) {
            errors.add("目的城市不能为空");
        }
        if (request.getType() == null || request.getType().trim().isEmpty()) {
            errors.add("交通类型不能为空");
        }
        if (request.getRouteNo() == null || request.getRouteNo().trim().isEmpty()) {
            errors.add("路线编号不能为空");
        }
        
        // 如果有必填字段缺失，直接返回
        if (!errors.isEmpty()) {
            response.setValid(false);
            response.setErrors(errors);
            return Result.success(response);
        }

        // 1. 基础验证
        if (request.getFromCity() != null && request.getFromCity().equals(request.getToCity())) {
            errors.add("出发城市和目的城市不能相同");
            response.setValid(false);
            response.setErrors(errors);
            return Result.success(response);
        }

        // 基础验证
        if (!routeManageService.exists(
                request.getFromCity(), 
                request.getToCity(),
                request.getRouteNo(),
                request.getDeparture())) {
            errors.add("路线已存在");
        }

        // 时间验证
        try {
            LocalTime departure = LocalTime.parse(request.getDeparture());
            LocalTime arrival = LocalTime.parse(request.getArrival());
            if (departure.equals(arrival)) {
                errors.add("出发时间和到达时间不能相同");
            }
        } catch (Exception e) {
            errors.add("时间格式不正确");
        }

        // 路线验证
        if (errors.isEmpty() && routeManageService.exists(
                request.getFromCity(), 
                request.getToCity(),
                request.getRouteNo(),
                request.getDeparture())) {
            errors.add("路线已存在");
        }

        response.setValid(errors.isEmpty());
        response.setErrors(errors);
        if (errors.isEmpty()) {
            response.setFormattedRoute(formatRoute(request));
        }

        return Result.success(response);
    }

    private String formatRoute(RouteRequest request) {
        return String.format("%s -> %s: %s(%s) %s-%s %.1f元",
            request.getFromCity(),
            request.getToCity(),
            request.getType(),
            request.getRouteNo(),
            request.getDeparture(),
            request.getArrival(),
            request.getFare());
    }
} 