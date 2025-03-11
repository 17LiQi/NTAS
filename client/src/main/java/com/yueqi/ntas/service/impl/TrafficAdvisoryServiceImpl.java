// package com.yueqi.ntas.service.impl;

// import com.yueqi.ntas.domain.entity.Edge;
// import com.yueqi.ntas.domain.entity.Vertex;
// import com.yueqi.ntas.domain.request.RouteRequest;
// import com.yueqi.ntas.service.TrafficAdvisoryService;
// import com.yueqi.ntas.exception.BusinessException;
// import com.yueqi.ntas.domain.response.OptimalRouteResponse;
// import com.yueqi.ntas.util.TimeUtils;
// import org.springframework.stereotype.Service;
// import javax.annotation.PostConstruct;
// import java.io.*;
// import java.util.*;
// import java.util.stream.Collectors;
// import lombok.extern.slf4j.Slf4j;

// @Service
// @Slf4j
// public class TrafficAdvisoryServiceImpl implements TrafficAdvisoryService {
//     private static final String DATA_FILE = "src/main/resources/routes.txt";
//     private Vertex[] vertices;      // 顶点数组
//     private int vertexCount;        // 实际顶点数
//     private Map<String, Integer> cityIndex;  // 城市名到索引的映射

//     @PostConstruct
//     public void init() {
//         vertices = new Vertex[100];
//         vertexCount = 0;
//         cityIndex = new HashMap<>();
//         loadFromFile(DATA_FILE);
//     }

//     private void initializeFromFile() {
//         File file = new File(DATA_FILE);
//         if (!file.exists()) {
//             try {
//                 file.getParentFile().mkdirs();
//                 file.createNewFile();
//                 // 写入初始数据
//                 try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//                     writer.write("0"); // 城市数量
//                     writer.newLine();
//                     writer.write("0"); // 路线数量
//                     writer.newLine();
//                 }
//             } catch (IOException e) {
//                 System.err.println("创建文件失败: " + e.getMessage());
//                 return;
//             }
//         }
//         loadFromFile(DATA_FILE);
//     }

//     @Override
//     public void addCity(String cityName) {
//         // 先检查城市是否已存在
//         if (cityIndex.containsKey(cityName)) {
//             throw new BusinessException("城市已存在");
//         }

//         // 检查数组容量
//         if (vertexCount >= vertices.length) {
//             // 扩容
//             Vertex[] newVertices = new Vertex[vertices.length * 2];
//             System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
//             vertices = newVertices;
//         }

//         // 添加新城市
//         vertices[vertexCount] = new Vertex(cityName, null);
//         cityIndex.put(cityName, vertexCount);
//         vertexCount++;

//         // 保存到文件
//         saveToFile();
        
//         // 打印日志
//         log.info("添加城市成功: {}, 当前城市总数: {}", cityName, vertexCount);
//     }

//     @Override
//     public boolean cityExists(String cityName) {
//         System.out.println("检查城市是否存在: " + cityName);
//         System.out.println("当前所有城市: " + cityIndex.keySet());
//         return cityIndex.containsKey(cityName);
//     }

//     @Override
//     public void deleteCity(String cityName) {
//         log.info("开始删除城市: {}", cityName);
        
//         if (!cityExists(cityName)) {
//             throw new BusinessException("城市不存在: " + cityName);
//         }

//         int cityIdx = cityIndex.get(cityName);
//         log.info("城市索引: {}", cityIdx);

//         // 删除所有与该城市相关的路线
//         for (int i = 0; i < vertexCount; i++) {
//             if (vertices[i] != null) {
//                 Edge prev = null;
//                 Edge current = vertices[i].getFirstEdge();
                
//                 // 删除以该城市为终点的路线
//                 while (current != null) {
//                     if (current.getTo() == cityIdx) {
//                         if (prev == null) {
//                             vertices[i].setFirstEdge(current.next);
//                         } else {
//                             prev.next = current.next;
//                         }
//                         log.info("删除路线: {} -> {}", vertices[i].getName(), cityName);
//                     } else {
//                         prev = current;
//                     }
//                     current = current.next;
//                 }
//             }
//         }

//         // 删除该城市的所有出边
//         vertices[cityIdx] = null;
//         cityIndex.remove(cityName);

//         // 压缩顶点数组并更新索引
//         compressVertexArray();

//         // 保存更改到文件
//         saveToFile();
        
//         log.info("城市删除成功: {}", cityName);
//     }

//     private void compressVertexArray() {
//         log.info("开始压缩顶点数组");
        
//         // 创建新的映射和顶点数组
//         Map<String, Integer> newCityIndex = new HashMap<>();
//         Vertex[] newVertices = new Vertex[vertices.length];
//         int newCount = 0;

//         // 重新组织顶点数组
//         for (int i = 0; i < vertexCount; i++) {
//             if (vertices[i] != null) {
//                 newVertices[newCount] = vertices[i];
//                 newCityIndex.put(vertices[i].getName(), newCount);
//                 newCount++;
//             }
//         }

//         // 更新所有边的目标索引
//         for (int i = 0; i < newCount; i++) {
//             Edge current = newVertices[i].getFirstEdge();
//             while (current != null) {
//                 String toCity = vertices[current.getTo()].getName();
//                 current.setToIndex(newCityIndex.get(toCity));
//                 current = current.next;
//             }
//         }

//         // 更新类的成员变量
//         vertices = newVertices;
//         cityIndex = newCityIndex;
//         vertexCount = newCount;
        
//         log.info("顶点数组压缩完成，当前城市数量: {}", vertexCount);
//     }

//     @Override
//     public void addRoute(RouteRequest request) {
//         if (!cityExists(request.getFromCity()) || !cityExists(request.getToCity())) {
//             throw new IllegalArgumentException("城市不存在");
//         }

//         int fromIdx = cityIndex.get(request.getFromCity());
//         int toIdx = cityIndex.get(request.getToCity());

//         Edge newEdge = new Edge(toIdx, request.getType(), request.getRouteNo(), 
//                 request.getDeparture(), request.getArrival(), request.getFare(), 
//                 vertices[fromIdx].getFirstEdge());
//         newEdge.setFromCity(request.getFromCity());
//         newEdge.setToCity(request.getToCity());
//         vertices[fromIdx].setFirstEdge(newEdge);

//         saveToFile();
//     }

//     @Override
//     public void deleteRoute(String from, String to, String routeNo, String departure) {
//         if (!cityIndex.containsKey(from) || !cityIndex.containsKey(to)) {
//             throw new IllegalArgumentException("城市不存在");
//         }

//         int fromIdx = cityIndex.get(from);
//         int toIdx = cityIndex.get(to);

//         Edge prev = null;
//         Edge current = vertices[fromIdx].getFirstEdge();
//         boolean found = false;

//         while (current != null) {
//             if (current.getTo() == toIdx &&
//                     current.getRouteNo().equals(routeNo) &&
//                     current.getDeparture().equals(departure)) {
//                 if (prev == null) {
//                     vertices[fromIdx].setFirstEdge(current.getNext());
//                 } else {
//                     prev.setNext(current.getNext());
//                 }
//                 found = true;
//                 break;
//             }
//             prev = current;
//             current = current.getNext();
//         }

//         if (!found) {
//             throw new IllegalArgumentException("未找到指定路线");
//         }

//         saveToFile();
//     }

//     @Override
//     public List<Edge> findAllRoutes() {
//         List<Edge> allRoutes = new ArrayList<>();
//         for (int i = 0; i < vertexCount; i++) {
//             Edge current = vertices[i].getFirstEdge();
//             while (current != null) {
//                 Edge routeInfo = new Edge(
//                         current.getTo(),
//                         current.getType(),
//                         current.getRouteNo(),
//                         current.getDeparture(),
//                         current.getArrival(),
//                         current.getCost(),
//                         null
//                 );
//                 routeInfo.setFromCity(vertices[i].getName());
//                 routeInfo.setToCity(vertices[current.getTo()].getName());
//                 allRoutes.add(routeInfo);
//                 current = current.getNext();
//             }
//         }
//         return allRoutes;
//     }

//     @Override
//     public List<Edge> findDirectRoutes(String from, String to) {
//         if (from == null || to == null) {
//             return findAllRoutes();
//         }

//         if (!cityIndex.containsKey(from) || !cityIndex.containsKey(to)) {
//             throw new IllegalArgumentException("城市不存在");
//         }

//         int fromIdx = cityIndex.get(from);
//         int toIdx = cityIndex.get(to);
//         List<Edge> directRoutes = new ArrayList<>();
//         Edge current = vertices[fromIdx].getFirstEdge();

//         while (current != null) {
//             if (current.getTo() == toIdx) {
//                 Edge routeInfo = new Edge(
//                         current.getTo(),
//                         current.getType(),
//                         current.getRouteNo(),
//                         current.getDeparture(),
//                         current.getArrival(),
//                         current.getCost(),
//                         null
//                 );
//                 routeInfo.setFromCity(from);
//                 routeInfo.setToCity(to);
//                 directRoutes.add(routeInfo);
//             }
//             current = current.getNext();
//         }

//         return directRoutes;
//     }

//     @Override
//     public List<Edge> findOptimalRoute(String start, String end, String criterion) {
//         if (!cityIndex.containsKey(start) || !cityIndex.containsKey(end)) {
//             throw new IllegalArgumentException("城市不存在");
//         }

//         int startIdx = cityIndex.get(start);
//         int endIdx = cityIndex.get(end);

//         // 初始化数组
//         double[] dist = new double[vertexCount];
//         int[] prev = new int[vertexCount];
//         int[] arrivalTimes = new int[vertexCount];
//         Edge[] selectedEdges = new Edge[vertexCount];
//         boolean[] visited = new boolean[vertexCount];

//         // 初始化距离和前驱数组
//         Arrays.fill(dist, Double.MAX_VALUE);
//         Arrays.fill(prev, -1);
//         Arrays.fill(arrivalTimes, 0);
//         dist[startIdx] = 0;

//         // 使用优先队列进行最短路径查找
//         PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) -> 
//                 Double.compare(dist[a[0]], dist[b[0]]));
//         pq.offer(new int[]{startIdx, 0});

//         while (!pq.isEmpty()) {
//             int[] current = pq.poll();
//             int u = current[0];

//             if (visited[u]) continue;
//             visited[u] = true;

//             Edge edge = vertices[u].getFirstEdge();
//             while (edge != null) {
//                 int v = edge.getTo();
//                 double weight;

//                 // 根据决策原则计算权重
//                 if (criterion.equals("time")) {
//                     // 对于起始站，不计算等待时间
//                     int lastArrivalTime = 0;
//                     if (prev[u] != -1) {
//                         Edge lastEdge = selectedEdges[u];
//                         lastArrivalTime = lastEdge.timeToMinutes(lastEdge.getArrival());
//                     }
//                     weight = edge.getTotalDuration(lastArrivalTime, u == startIdx);
//                 } else {
//                     weight = edge.getCost();
//                 }

//                 if (dist[u] + weight < dist[v]) {
//                     dist[v] = dist[u] + weight;
//                     prev[v] = u;
//                     selectedEdges[v] = edge;
//                     if (criterion.equals("time")) {
//                         arrivalTimes[v] = (edge.timeToMinutes(edge.getArrival())) % (24 * 60);
//                     }
//                     pq.offer(new int[]{v, 0});
//                 }
//                 edge = edge.next;
//             }
//         }

//         // 如果无法到达终点
//         if (dist[endIdx] == Double.MAX_VALUE) {
//             return Collections.emptyList();
//         }

//         // 构建路径
//         List<Edge> path = new ArrayList<>();
//         int current = endIdx;
//         while (prev[current] != -1) {
//             Edge edge = selectedEdges[current];
//             Edge pathEdge = new Edge(
//                 edge.getTo(),
//                 edge.getType(),
//                 edge.getRouteNo(),
//                 edge.getDeparture(),
//                 edge.getArrival(),
//                 edge.getCost(),
//                 null
//             );
//             // 设置城市信息
//             pathEdge.setFromCity(vertices[prev[current]].getName());
//             pathEdge.setToCity(vertices[current].getName());
            
//             // 计算实际等待时间
//             if (prev[prev[current]] != -1) {
//                 Edge prevEdge = selectedEdges[prev[current]];
//                 int lastArrivalTime = prevEdge.timeToMinutes(prevEdge.getArrival());
//                 int departureTime = edge.timeToMinutes(edge.getDeparture());
                
//                 // 计算等待时间（考虑跨天情况）
//                 if (departureTime < lastArrivalTime) {
//                     departureTime += 24 * 60;
//                 }
//             }
            
//             path.add(0, pathEdge);
//             current = prev[current];
//         }

//         return path;
//     }

//     @Override
//     public List<String> getAllCities() {
//         List<String> cities = new ArrayList<>();
//         for (int i = 0; i < vertexCount; i++) {
//             if (vertices[i] != null) {
//                 cities.add(vertices[i].getName());
//             }
//         }
//         return cities;
//     }

//     @Override
//     public List<String> findAdjacentCities(String fromCity) {
//         System.out.println("查找城市 " + fromCity + " 的相邻站点");
//         Set<String> adjacentCities = new HashSet<>();
        
//         List<Edge> allRoutes = findAllRoutes();
//         System.out.println("所有路线数量: " + allRoutes.size());
        
//         for (Edge route : allRoutes) {
//             System.out.println("检查路线: " + route.getFromCity() + " -> " + route.getToCity());
//             if (route.getFromCity().equals(fromCity)) {
//                 adjacentCities.add(route.getToCity());
//                 System.out.println("添加相邻城市: " + route.getToCity());
//             }
//         }
        
//         List<String> result = new ArrayList<>(adjacentCities);
//         System.out.println("找到的相邻城市: " + result);
//         return result;
//     }

//     private void loadFromFile(String filename) {
//         log.info("开始加载文件: {}", filename);
//         try {
//             File file = new File(filename);
//             if (!file.exists()) {
//                 log.info("文件不存在，初始化新文件");
//                 initializeFromFile();
//                 return;
//             }

//             // 先清空现有数据
//             vertices = new Vertex[100];
//             vertexCount = 0;
//             cityIndex.clear();

//             BufferedReader reader = new BufferedReader(new FileReader(filename));
//             String line;

//             // 第一行：城市数量
//             line = reader.readLine();
//             if (line == null || line.trim().isEmpty()) {
//                 log.warn("文件为空");
//                 return;
//             }

//             int cityCount = Integer.parseInt(line.trim());
//             log.info("读取到城市数量: {}", cityCount);

//             // 读取城市名称
//             for (int i = 0; i < cityCount; i++) {
//                 String cityName = reader.readLine().trim();
//                 if (!cityName.isEmpty()) {
//                     vertices[vertexCount] = new Vertex(cityName, null);
//                     cityIndex.put(cityName, vertexCount);
//                     vertexCount++;
//                     log.info("读取到城市: {}", cityName);
//                 }
//             }

//             // 跳过空行
//             reader.readLine();

//             // 读取路线信息
//             while ((line = reader.readLine()) != null) {
//                 line = line.trim();
//                 if (!line.isEmpty()) {
//                     String[] parts = line.split(",");
//                     if (parts.length >= 7) {
//                         try {
//                             String fromCity = parts[0].trim();
//                             String toCity = parts[1].trim();

//                             if (cityIndex.containsKey(fromCity) && cityIndex.containsKey(toCity)) {
//                                 int fromIdx = cityIndex.get(fromCity);
//                                 int toIdx = cityIndex.get(toCity);

//                                 Edge edge = new Edge(
//                                     toIdx,
//                                     parts[2].trim(),  // type
//                                     parts[3].trim(),  // routeNo
//                                     parts[4].trim(),  // departure
//                                     parts[5].trim(),  // arrival
//                                     Double.parseDouble(parts[6].trim()),  // fare
//                                     vertices[fromIdx].getFirstEdge()  // next
//                                 );
//                                 edge.setFromCity(fromCity);
//                                 edge.setToCity(toCity);
//                                 vertices[fromIdx].setFirstEdge(edge);
//                                 log.info("读取到路线: {} -> {}", fromCity, toCity);
//                             }
//                         } catch (NumberFormatException e) {
//                             log.error("解析票价失败: {}", parts[6]);
//                         }
//                     }
//                 }
//             }

//             log.info("城市加载完成，当前城市列表: {}", cityIndex.keySet());
//             reader.close();
//         } catch (IOException e) {
//             log.error("读取文件失败: {}", e.getMessage(), e);
//             throw new BusinessException("读取文件失败");
//         }
//     }

//     private void initializeEmptyFile(File file) {
//         try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
//             writer.write("0"); // 城市数量
//             writer.newLine();
//             writer.newLine(); // 空行
//             writer.newLine(); // 路线数据起始位置
//         } catch (IOException e) {
//             System.err.println("初始化文件失败: " + e.getMessage());
//         }
//     }

//     private void saveToFile() {
//         log.info("开始保存文件，当前城市数量: {}", vertexCount);
//         try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
//             // 写入城市数量
//             writer.write(String.valueOf(vertexCount));
//             writer.newLine();
//             log.info("写入城市数量: {}", vertexCount);

//             // 写入城市列表
//             for (int i = 0; i < vertexCount; i++) {
//                 if (vertices[i] != null) {
//                     writer.write(vertices[i].getName());
//                     writer.newLine();
//                     log.info("写入城市: {}", vertices[i].getName());
//                 }
//             }

//             // 写入空行
//             writer.newLine();
//             log.info("城市列表写入完成");

//             // 写入路线信息
//             int routeCount = 0;
//             for (int i = 0; i < vertexCount; i++) {
//                 if (vertices[i] != null) {
//                     Edge current = vertices[i].getFirstEdge();
//                     while (current != null) {
//                         writer.write(String.format("%s,%s,%s,%s,%s,%s,%.2f",
//                                 vertices[i].getName(),
//                                 vertices[current.getTo()].getName(),
//                                 current.getType(),
//                                 current.getRouteNo(),
//                                 current.getDeparture(),
//                                 current.getArrival(),
//                                 current.getFare()));
//                         writer.newLine();
//                         routeCount++;
//                         current = current.getNext();
//                     }
//                 }
//             }
//             log.info("路线信息写入完成，共写入 {} 条路线", routeCount);
            
//             writer.flush();
//             log.info("文件保存成功");
//         } catch (IOException e) {
//             log.error("保存文件失败: {}", e.getMessage(), e);
//             throw new BusinessException("保存文件失败");
//         }
//     }

//     // 在 Service 中添加格式化方法
//     private String formatDuration(int minutes) {
//         int days = minutes / (24 * 60);
//         int hours = (minutes % (24 * 60)) / 60;
//         int mins = minutes % 60;

//         if (days > 0) {
//             return String.format("%d天%d小时%d分钟", days, hours, mins);
//         }
//         return String.format("%d小时%d分钟", hours, mins);
//     }

//     @Override
//     public List<Edge> findDirectRoutesFromCity(String fromCity) {
//         if (!cityExists(fromCity)) {
//             throw new IllegalArgumentException("城市不存在");
//         }

//         return findAllRoutes().stream()
//                 .filter(route -> route.getFromCity().equals(fromCity))
//                 .collect(Collectors.toList());
//     }

//     public int calculateTotalDuration(Edge edge, int currentTime, boolean isStartingPoint) {
//         int departureMinutes = TimeUtils.timeToMinutes(edge.getDeparture());
//         if (isStartingPoint || currentTime <= 0) {
//             return edge.calculateDuration();
//         }
        
//         int waitTime = calculateWaitTime(currentTime, departureMinutes);
//         return waitTime + edge.calculateDuration();
//     }

//     private int calculateWaitTime(int currentTime, int departureMinutes) {
//         return currentTime > departureMinutes ?
//             24 * 60 - currentTime + departureMinutes :
//             departureMinutes - currentTime;
//     }
// }