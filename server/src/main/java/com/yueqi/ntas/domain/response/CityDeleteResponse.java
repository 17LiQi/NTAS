package com.yueqi.ntas.domain.response;

import com.yueqi.ntas.domain.dto.RouteDisplayDTO;
import lombok.Data;
import java.util.List;

@Data
public class CityDeleteResponse {
    private int relatedRoutesCount;
    private String confirmMessage;
    private List<RouteDisplayDTO> relatedRoutes;
} 