package com.yueqi.ntas.domain.request;

import lombok.Data;

@Data
public class RouteDeleteRequest {
    private String fromCity;
    private String toCity;
    private String routeNo;
    private String departure;
} 