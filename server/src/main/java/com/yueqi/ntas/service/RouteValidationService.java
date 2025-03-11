package com.yueqi.ntas.service;

import com.yueqi.ntas.domain.request.RouteRequest;
import com.yueqi.ntas.domain.response.ValidationResponse;

public interface RouteValidationService {
    ValidationResponse validateRoute(RouteRequest request);
    ValidationResponse validateRouteForDeletion(String from, String to, String routeNo, String departure);
} 