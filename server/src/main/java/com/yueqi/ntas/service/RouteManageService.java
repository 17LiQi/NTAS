package com.yueqi.ntas.service;

import com.yueqi.ntas.domain.request.RouteRequest;

public interface RouteManageService {
    void addRoute(RouteRequest request);
    void deleteRoute(String from, String to, String routeNo, String departure);
    boolean exists(String from, String to, String routeNo, String departure);
} 