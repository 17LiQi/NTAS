// package com.yueqi.ntas.controller;

// import com.yueqi.ntas.common.Result;
// import com.yueqi.ntas.domain.dto.RouteDTO;
// import com.yueqi.ntas.domain.dto.RouteDisplayDTO;
// import com.yueqi.ntas.domain.entity.Edge;
// import com.yueqi.ntas.domain.request.RouteDeleteRequest;
// import com.yueqi.ntas.domain.request.RouteRequest;
// import com.yueqi.ntas.domain.response.OptimalRouteResponse;
// import com.yueqi.ntas.domain.response.OptimalRouteResponse.TransferInfo;
// import com.yueqi.ntas.service.TrafficAdvisoryService;
// import com.yueqi.ntas.service.RouteService;
// import com.yueqi.ntas.service.CityService;
// import com.yueqi.ntas.util.PinyinHelper;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.web.bind.annotation.*;
// import lombok.extern.slf4j.Slf4j;

// import java.util.ArrayList;
// import java.util.List;
// import java.util.Map;
// import java.util.stream.Collectors;
// import java.time.LocalTime;
// import java.util.stream.Stream;

// @Slf4j
// @RestController
// @RequestMapping("/traffic")
// @CrossOrigin(originPatterns = "http://localhost:*", allowedHeaders = "*", allowCredentials = "true")  // 添加跨域支持
// public class TrafficAdvisoryController {

//     @Autowired
//     private TrafficAdvisoryService trafficAdvisoryService;

//     @Autowired
//     private RouteService routeService;

//     @Autowired
//     private CityService cityService;

//     // 获取所有路线
//     @GetMapping("/routes")
//     public Result<List<RouteDTO>> getAllRoutes() {
//         try {
//             List<Edge> routes = trafficAdvisoryService.findAllRoutes();
//             List<RouteDTO> dtos = routes.stream()
//                     .map(route -> {
//                         RouteDTO dto = new RouteDTO();
//                         dto.setFromCity(route.getFromCity());
//                         dto.setToCity(route.getToCity());
//                         dto.setType(route.getType());
//                         dto.setRouteNo(route.getRouteNo());
//                         dto.setDeparture(route.getDeparture());
//                         dto.setArrival(route.getArrival());
//                         dto.setFormattedFare(String.format("%.1f 元", route.getFare()));
//                         return dto;
//                     })
//                     .collect(Collectors.toList());
//             return Result.success(dtos);
//         } catch (Exception e) {
//             return Result.error(e.getMessage());
//         }
//     }

//     // 获取直达路线
//     @GetMapping("/route/direct")
//     public Result<List<RouteDisplayDTO>> findDirectRoutes(
//             @RequestParam(required = false) String from,
//             @RequestParam(required = false) String to) {
//         try {
//             List<Edge> routes;
//             if ((from == null || from.isEmpty() || "ALL".equals(from)) &&
//                     (to == null || to.isEmpty() || "ALL".equals(to))) {
//                 routes = trafficAdvisoryService.findAllRoutes();
//             } else {
//                 routes = trafficAdvisoryService.findDirectRoutes(from, to);
//             }
//             List<RouteDisplayDTO> displayRoutes = routes.stream()
//                     .map(RouteDisplayDTO::new)
//                     .collect(Collectors.toList());
//             return Result.success(displayRoutes);
//         } catch (Exception e) {
//             return Result.error(e.getMessage());
//         }
//     }

//     // 查询最优路径
//     @GetMapping("/route/optimal")
//     public Result<OptimalRouteResponse> findOptimalRoute(
//             @RequestParam String start,
//             @RequestParam String end,
//             @RequestParam String criterion) {
//         try {
//             List<Edge> routes = trafficAdvisoryService.findOptimalRoute(start, end, criterion);
//             OptimalRouteResponse result = calculateRouteDetails(routes);
//             return Result.success(result);
//         } catch (Exception e) {
//             return Result.error(e.getMessage());
//         }
//     }

//     private OptimalRouteResponse calculateRouteDetails(List<Edge> routes) {
//         OptimalRouteResponse result = new OptimalRouteResponse();
//         result.setRoutes(routes);

//         // 计算总费用
//         double totalCost = routes.stream()
//                 .mapToDouble(Edge::getCost)
//                 .sum();
//         result.setTotalCost(totalCost);

//         // 计算路径
//         String routePath = routes.stream()
//                 .map(Edge::getFromCity)
//                 .reduce((a, b) -> a + " → " + b)
//                 .orElse("");
//         if (!routes.isEmpty()) {
//             routePath += " → " + routes.get(routes.size() - 1).getToCity();
//         }
//         result.setRoutePath(routePath);

//         // 计算总时间和中转信息
//         List<TransferInfo> transfers = new ArrayList<>();
//         int totalMinutes = 0;

//         for (int i = 0; i < routes.size(); i++) {
//             Edge route = routes.get(i);
//             // 计算单程时间
//             int routeMinutes = calculateTimeDifference(route.getDeparture(), route.getArrival());
//             totalMinutes += routeMinutes;

//             // 计算中转等待时间
//             if (i < routes.size() - 1) {
//                 Edge nextRoute = routes.get(i + 1);
//                 int waitMinutes = calculateTimeDifference(route.getArrival(), nextRoute.getDeparture());
//                 totalMinutes += waitMinutes;

//                 TransferInfo transfer = new TransferInfo();
//                 transfer.setCity(route.getToCity());
//                 transfer.setWaitTime(formatDuration(waitMinutes));
//                 transfers.add(transfer);
//             }
//         }

//         result.setTotalDuration(formatDuration(totalMinutes));
//         result.setTransfers(transfers);

//         return result;
//     }

//     private int calculateTimeDifference(String time1, String time2) {
//         String[] t1Parts = time1.split(":");
//         String[] t2Parts = time2.split(":");
//         int minutes1 = Integer.parseInt(t1Parts[0]) * 60 + Integer.parseInt(t1Parts[1]);
//         int minutes2 = Integer.parseInt(t2Parts[0]) * 60 + Integer.parseInt(t2Parts[1]);

//         if (minutes2 < minutes1) {
//             minutes2 += 24 * 60; // 跨天处理
//         }
//         return minutes2 - minutes1;
//     }

//     private String formatDuration(int minutes) {
//         int hours = minutes / 60;
//         int mins = minutes % 60;
//         return String.format("%d小时%d分钟", hours, mins);
//     }

//     // 添加城市
//     @PostMapping("/city")
//     public Result<Void> addCity(@RequestParam String cityName) {
//         try {
//             trafficAdvisoryService.addCity(cityName);
//             return Result.success(null);
//         } catch (Exception e) {
//             return Result.error(e.getMessage());
//         }
//     }

//     // 删除城市
//     @PostMapping("/city/delete")
//     public Result<Void> deleteCity(@RequestParam String cityName) {
//         log.info("接收到删除城市请求: {}", cityName);
//         try {
//             trafficAdvisoryService.deleteCity(cityName);
//             return Result.success(null);
//         } catch (Exception e) {
//             log.error("删除城市失败", e);
//             return Result.error(e.getMessage());
//         }
//     }

//     // 添加路线
//     @PostMapping("/route")
//     public Result<Void> addRoute(@RequestBody RouteRequest request) {
//         trafficAdvisoryService.addRoute(request);
//         return Result.success(null);
//     }

//     // 删除路线
//     @PostMapping("/route/delete")
//     public Result<Void> deleteRoute(@RequestBody RouteDeleteRequest request) {
//         log.info("接收到删除路线请求: {}", request);
//         try {
//             trafficAdvisoryService.deleteRoute(
//                 request.getFromCity(),
//                 request.getToCity(), 
//                 request.getRouteNo(),
//                 request.getDeparture()
//             );
//             return Result.success(null);
//         } catch (Exception e) {
//             log.error("删除路线失败", e);
//             return Result.error(e.getMessage());
//         }
//     }

//     @GetMapping("/route/delete/info")
//     public Result<RouteDeleteInfo> getRouteDeleteInfo(
//             @RequestParam String fromCity,
//             @RequestParam String toCity,
//             @RequestParam String type,
//             @RequestParam String flightNo) {
//         RouteDeleteInfo info = new RouteDeleteInfo();
//         info.setRouteDescription(
//                 String.format("从 %s 到 %s 的 %s%s 路线", fromCity, toCity, type, flightNo)
//         );
//         info.setConfirmMessage("确定要删除该路线吗？");
//         return Result.success(info);
//     }

//     @GetMapping("/cities")
//     public Result<List<String>> getCities(@RequestParam(required = false) String query) {
//         try {
//             List<String> allCities = cityService.getAllCities();
//             if (query != null && !query.isEmpty()) {
//                 return Result.success(allCities.stream()
//                         .filter(city -> city.toLowerCase().contains(query.toLowerCase()))
//                         .sorted()
//                         .collect(Collectors.toList()));
//             }
//             return Result.success(allCities.stream()
//                     .sorted()
//                     .collect(Collectors.toList()));
//         } catch (Exception e) {
//             log.error("获取城市列表失败", e);
//             return Result.error(e.getMessage());
//         }
//     }

//     // 在 Controller 中修改返回类型
//     @GetMapping("/routes/display")
//     public Result<List<RouteDisplayDTO>> getRoutesForDisplay() {
//         List<Edge> routes = trafficAdvisoryService.findAllRoutes();
//         List<RouteDisplayDTO> displayRoutes = routes.stream()
//                 .map(RouteDisplayDTO::new)
//                 .collect(Collectors.toList());
//         return Result.success(displayRoutes);
//     }

//     private RouteDisplayDTO convertToDisplayDTO(Edge edge) {
//         return new RouteDisplayDTO(edge);  // 使用 RouteDisplayDTO 的构造函数
//     }

//     @GetMapping("/adjacent-cities")
//     public Result<List<String>> getAdjacentCities(@RequestParam String fromCity) {
//         try {
//             if (!trafficAdvisoryService.cityExists(fromCity)) {
//                 return Result.error("城市不存在");
//             }
//             List<String> adjacentCities = trafficAdvisoryService.findAdjacentCities(fromCity);
//             return Result.success(adjacentCities);
//         } catch (Exception e) {
//             return Result.error(e.getMessage());
//         }
//     }


//     // 获取路线删除确认信息
//     @GetMapping("/route/delete/confirm")
//     public Result<OptimalRouteResponse.RouteDeleteConfirmDTO> getRouteDeleteConfirm(
//             @RequestParam String fromCity,
//             @RequestParam String toCity,
//             @RequestParam String type,
//             @RequestParam String flightNo) {

//         OptimalRouteResponse.RouteDeleteConfirmDTO dto = new OptimalRouteResponse.RouteDeleteConfirmDTO();
//         dto.setRouteDescription(
//                 String.format("从 %s 到 %s 的 %s%s 路线", fromCity, toCity, type, flightNo)
//         );
//         dto.setConfirmMessage("确定要删除该路线吗？");
//         return Result.success(dto);
//     }

//     // 验证路线
//     @PostMapping("/validate-route")
//     public Result<RouteValidationDTO> validateRoute(@RequestBody RouteRequest request) {
//         RouteValidationDTO validationResult = new RouteValidationDTO();
//         List<String> errors = new ArrayList<>();

//         // 1. 基础验证
//         if (!trafficAdvisoryService.cityExists(request.getFromCity()) || 
//             !trafficAdvisoryService.cityExists(request.getToCity())) {
//             errors.add("出发城市或目的城市不存在");
//         }

//         if (request.getFromCity().equals(request.getToCity())) {
//             errors.add("出发城市和目的城市不能相同");
//         }

//         // 2. 时间验证
//         LocalTime departure = LocalTime.parse(request.getDeparture());
//         LocalTime arrival = LocalTime.parse(request.getArrival());

//         if (departure.equals(arrival)) {
//             errors.add("出发时间和到达时间不能相同");
//         }

//         // 3. 路线唯一性验证
//         if (routeService.exists(request.getFromCity(), request.getToCity(),
//                 request.getRouteNo(), request.getDeparture())) {
//             errors.add("该路线已存在");
//         }

//         // 4. 交通类型与航班号/车次格式验证
//         if (!validateTransportTypeAndNumber(request.getType(), request.getRouteNo())) {
//             errors.add("航班号/车次格式不正确");
//         }

//         // 5. 费用验证
//         if (request.getFare() <= 0) {
//             errors.add("费用必须大于0");
//         }

//         validationResult.setValid(errors.isEmpty());
//         validationResult.setErrors(errors);

//         if (errors.isEmpty()) {
//             // 格式化并返回验证后的路线信息
//             validationResult.setValidatedRoute(formatRouteInfo(request));
//         }

//         return Result.success(validationResult);
//     }

//     private Route formatRouteInfo(RouteRequest request) {
//         // 格式化路线信息，统一格式
//         Route route = new Route();
//         route.setFromCity(request.getFromCity().trim());
//         route.setToCity(request.getToCity().trim());
//         route.setType(request.getType());
//         route.setRouteNo(request.getRouteNo().toUpperCase());  // 统一大写
//         route.setDeparture(request.getDeparture());
//         route.setArrival(request.getArrival());
//         route.setFare(request.getFare());
//         return route;
//     }

//     private boolean validateTransportTypeAndNumber(String type, String number) {
//         if ("飞机".equals(type)) {
//             // 航班号格式验证：2个大写字母后跟3-4个数字
//             return number.matches("[A-Z]{2}\\d{3,4}");
//         } else if ("火车".equals(type)) {
//             // 车次号格式验证：字母+数字组合
//             return number.matches("[GDZCK]\\d+");
//         }
//         return false;
//     }

//     // 获取过滤后的城市列表
//     @GetMapping("/cities/filter")
//     public Result<List<String>> getFilteredCities(
//             @RequestParam String query,
//             @RequestParam(required = false) String excludeCity,
//             @RequestParam(required = false) Integer limit
//     ) {
//         try {
//             // 1. 获取所有城市
//             List<String> allCities = cityService.getAllCities();

//             // 2. 应用过滤条件
//             Stream<String> filteredStream = allCities.stream()
//                     .filter(city -> {
//                         // 排除指定城市
//                         if (excludeCity != null && city.equals(excludeCity)) {
//                             return false;
//                         }

//                         // 模糊匹配：支持拼音首字母、全拼和中文
//                         if (query != null && !query.isEmpty()) {
//                             String pinyin = PinyinHelper.getFirstSpell(city);  // 获取拼音首字母
//                             String fullPinyin = PinyinHelper.getFullSpell(city);  // 获取全拼
//                             return city.contains(query) ||
//                                     pinyin.startsWith(query.toLowerCase()) ||
//                                     fullPinyin.startsWith(query.toLowerCase());
//                         }

//                         return true;
//                     });

//             // 3. 限制返回数量
//             if (limit != null && limit > 0) {
//                 filteredStream = filteredStream.limit(limit);
//             }

//             // 4. 收集结果
//             List<String> result = filteredStream.collect(Collectors.toList());

//             return Result.success(result);
//         } catch (Exception e) {
//             return Result.error("获取城市列表失败：" + e.getMessage());
//         }
//     }

//     private boolean isValidTimeFormat(String time) {
//         try {
//             if (time == null || !time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
//                 return false;
//             }
//             return true;
//         } catch (Exception e) {
//             return false;
//         }
//     }

//     private String formatRoute(RouteRequest request) {
//         return String.format("%s -> %s: %s(%s) %s-%s %.1f元",
//                 request.getFromCity(),
//                 request.getToCity(),
//                 request.getType(),
//                 request.getRouteNo(),
//                 request.getDeparture(),
//                 request.getArrival(),
//                 request.getFare());
//     }

//     @GetMapping("/city/validate")
//     public Result<CityValidationDTO> validateCity(@RequestParam String cityName) {
//         CityValidationDTO dto = new CityValidationDTO();
//         List<String> errors = new ArrayList<>();

//         // 验证城市名称格式
//         if (cityName == null || cityName.trim().isEmpty()) {
//             errors.add("城市名称不能为空");
//         } else {
//             cityName = cityName.trim();
//             if (cityName.length() > 10) {
//                 errors.add("城市名称过长");
//             }
//             if (!cityName.matches("^[\\u4e00-\\u9fa5]+$")) {
//                 errors.add("城市名称只能包含中文字符");
//             }
//         }

//         // 检查城市是否已存在
//         if (errors.isEmpty() && cityExists(cityName)) {
//             errors.add("城市已存在");
//         }

//         dto.setValid(errors.isEmpty());
//         dto.setErrors(errors);
//         dto.setFormattedCityName(cityName);

//         return Result.success(dto);
//     }

//     @GetMapping("/city/delete/info")
//     public Result<CityDeleteInfoDTO> getCityDeleteInfo(@RequestParam String cityName) {
//         CityDeleteInfoDTO dto = new CityDeleteInfoDTO();

//         // 统计相关路线
//         List<Edge> relatedRoutes = findRoutesByCity(cityName);
//         dto.setRelatedRoutesCount(relatedRoutes.size());

//         // 格式化提示信息
//         if (relatedRoutes.isEmpty()) {
//             dto.setConfirmMessage(String.format("确定要删除城市 \"%s\" 吗？", cityName));
//         } else {
//             dto.setConfirmMessage(
//                     String.format("删除城市 \"%s\" 将同时删除 %d 条相关路线，是否继续？",
//                             cityName, relatedRoutes.size())
//             );
//         }

//         // 添加相关路线详情
//         dto.setRelatedRoutes(relatedRoutes.stream()
//                 .map(this::convertToDisplayDTO)
//                 .collect(Collectors.toList()));

//         return Result.success(dto);
//     }

//     private boolean cityExists(String cityName) {
//         return trafficAdvisoryService.cityExists(cityName);
//     }

//     private List<Edge> findRoutesByCity(String cityName) {
//         return trafficAdvisoryService.findAllRoutes().stream()
//                 .filter(route -> route.getFromCity().equals(cityName) ||
//                         route.getToCity().equals(cityName))
//                 .collect(Collectors.toList());
//     }

//     @GetMapping("/adjacent-stations")
//     @ResponseBody
//     public Result<AdjacentStationsDTO> getAdjacentStations(@RequestParam String fromCity) {
//         try {
//             if (!trafficAdvisoryService.cityExists(fromCity)) {
//                 return Result.error("城市不存在");
//             }

//             AdjacentStationsDTO dto = new AdjacentStationsDTO();
//             List<String> adjacentStations = trafficAdvisoryService.findAdjacentCities(fromCity);
//             dto.setAdjacentStations(adjacentStations);

//             if (!adjacentStations.isEmpty()) {
//                 List<Edge> routes = trafficAdvisoryService.findDirectRoutesFromCity(fromCity);
//                 dto.setDirectRoutes(routes.stream()
//                         .map(RouteDisplayDTO::new)  // 使用构造函数
//                         .collect(Collectors.toList()));

//                 Map<String, Integer> routeCounts = routes.stream()
//                         .collect(Collectors.groupingBy(
//                                 Edge::getToCity,
//                                 Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
//                         ));
//                 dto.setRouteCounts(routeCounts);
//             }

//             dto.setMessage(adjacentStations.isEmpty() 
//                     ? "该城市没有相邻站点" 
//                     : String.format("找到 %d 个相邻站点", adjacentStations.size()));

//             return Result.success(dto);
//         } catch (Exception e) {
//             return Result.error(e.getMessage());
//         }
//     }

// }