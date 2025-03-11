package com.yueqi.ntas.domain.response;

import com.yueqi.ntas.domain.dto.RouteDisplayDTO;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AdjacentStationsResponse {
    private List<String> adjacentStations;
    private List<RouteDisplayDTO> directRoutes;
    private Map<String, Integer> routeCounts;
    private String message;
}
