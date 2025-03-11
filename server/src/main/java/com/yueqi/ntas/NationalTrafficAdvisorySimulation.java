package com.yueqi.ntas;

import java.io.*;
import java.util.*;

public class NationalTrafficAdvisorySimulation {
    // 边的结构(表示一段路程)
    static class Edge {
        int to;             // 邻接点(目标城市编号)
        String type;        // 交通类型(火车、飞机等)
        String flightNo;    // 班次号(如K7731,KN5638等)
        String departure;   // 出发时间 (HH:mm格式)
        String arrival;     // 到达时间 (HH:mm格式)
        double cost;       // 费用(改为double类型支持小数)
        Edge next;         // 指向下一条边

        // 将时间字符串转换为分钟数(从0点开始)
        public int timeToMinutes(String time) {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        }

        // 计算行程时间,包括跨天的情况
        private int calculateDuration() {
            int startMinutes = timeToMinutes(departure);
            int endMinutes = timeToMinutes(arrival);

            if (endMinutes < startMinutes) {  // 跨天的情况
                return (24 * 60 - startMinutes) + endMinutes;
            } else {
                return endMinutes - startMinutes;
            }
        }

        // 计算实际需要的时间(包括等待时间)
        public int getTotalDuration(int currentTime, boolean isStartingPoint) {
            int departureMinutes = timeToMinutes(departure);
            int waitTime = 0;

            // 只有非起始站才需要计算等待时间
            if (!isStartingPoint && currentTime > 0) {
                if (currentTime > departureMinutes) {
                    waitTime = 24 * 60 - currentTime + departureMinutes;
                } else {
                    waitTime = departureMinutes - currentTime;
                }
            }

            return waitTime + calculateDuration();
        }

        public Edge(int to, String type, String flightNo, String departure,
                    String arrival, double cost, Edge next) {
            this.to = to;
            this.type = type;
            this.flightNo = flightNo;
            this.departure = departure;
            this.arrival = arrival;
            this.cost = cost;
            this.next = next;
        }
    }

    // 顶点结构(城市)
    static class Vertex {
        String name;    // 城市名称
        Edge firstEdge; // 指向第一条边

        public Vertex(String name) {
            this.name = name;
            this.firstEdge = null;
        }
    }

    private Vertex[] vertices;      // 顶点数组
    private int vertexCount;        // 实际顶点数
    private Map<String, Integer> cityIndex;  // 城市名到索引的映射

    public NationalTrafficAdvisorySimulation(int maxCities) {
        vertices = new Vertex[maxCities];
        vertexCount = 0;
        cityIndex = new HashMap<>();
    }

    // 添加城市
    public void addCity(String cityName) {
        if (!cityIndex.containsKey(cityName)) {
            vertices[vertexCount] = new Vertex(cityName);
            cityIndex.put(cityName, vertexCount++);
            // 自动保存到文件
            saveToFile("src/com/yueqi/CurriculumDesign/routes.txt");
        }
    }

    // 添加交通路线
    public void addRoute(String from, String to, String type, String flightNo,
                         String departure, String arrival, double cost) {
        int fromIdx = cityIndex.get(from);
        int toIdx = cityIndex.get(to);

        // 添加边
        Edge newEdge = new Edge(toIdx, type, flightNo, departure, arrival,
                cost, vertices[fromIdx].firstEdge);
        vertices[fromIdx].firstEdge = newEdge;

        // 自动保存到文件
        saveToFile("src/com/yueqi/CurriculumDesign/routes.txt");
    }

    // 从文件读取数据
    public void loadFromFile(String filename) {
        try {
            // 先清空现有数据
            vertices = new Vertex[100];
            vertexCount = 0;
            cityIndex.clear();

            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;

            // 第一行:城市数量
            int cityCount = Integer.parseInt(reader.readLine().trim());

            // 读取城市名称
            for (int i = 0; i < cityCount; i++) {
                String cityName = reader.readLine().trim();
                if (!cityName.isEmpty()) {
                    addCity(cityName);
                }
            }

            // 跳过空行
            reader.readLine();

            // 读取路线信息
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split(",");
                    if (parts.length >= 7) {  // 修改为7个字段
                        try {
                            addRoute(
                                    parts[0].trim(),
                                    parts[1].trim(),
                                    parts[2].trim(),
                                    parts[3].trim(),  // 班次号
                                    parts[4].trim(),  // 出发时间
                                    parts[5].trim(),  // 到达时间
                                    Double.parseDouble(parts[6].trim())  // 票价
                            );
                        } catch (NumberFormatException e) {
                            System.err.println("解析票价失败:" + parts[6]);
                        }
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("读取文件失败:" + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("解析数字失败:" + e.getMessage());
        }
    }

    // 从键盘输入数据
    public void inputFromKeyboard() {
        Scanner sc = new Scanner(System.in);

        System.out.println("请输入城市数量:");
        int cityCount = sc.nextInt();
        sc.nextLine(); // 消耗换行符

        System.out.println("请依次输入城市名称:");
        for (int i = 0; i < cityCount; i++) {
            addCity(sc.nextLine().trim());
        }

        System.out.println("请输入路线数量:");
        int routeCount = sc.nextInt();
        sc.nextLine(); // 消耗换行符

        System.out.println("请依次输入路线信息(格式:起点,终点,类型,班次号,出发时间,到达时间,票价):");
        for (int i = 0; i < routeCount; i++) {
            String[] parts = sc.nextLine().split(",");
            if (parts.length == 7) {
                addRoute(parts[0], parts[1], parts[2], parts[3],
                        parts[4], parts[5], Double.parseDouble(parts[6]));
            }
        }
    }

    // 打印所有路线信息
    public void printAllRoutes() {
        System.out.println("\n=== 所有路线信息 ===");
        boolean hasRoutes = false;
        for (int i = 0; i < vertexCount; i++) {
            Edge edge = vertices[i].firstEdge;
            if (edge != null) {  // 只显示有路线的城市
                hasRoutes = true;
                System.out.println("\n从 " + vertices[i].name + " 出发的路线:");
                while (edge != null) {
                    System.out.printf("到 %s: %s %s, 时长%d分钟, %s->%s, 票价%.1f元\n",
                            vertices[edge.to].name,
                            edge.type,
                            edge.flightNo.isEmpty() ? "" : "(" + edge.flightNo + ")",
                            edge.getTotalDuration(0, false),
                            edge.departure, edge.arrival, edge.cost);
                    edge = edge.next;
                }
            }
        }
        if (!hasRoutes) {
            System.out.println("当前没有任何路线信息！");
        }
    }

    // 保存数据到文件
    public void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            // 写入城市数量
            writer.println(vertexCount);

            // 写入城市名称
            for (int i = 0; i < vertexCount; i++) {
                writer.println(vertices[i].name);
            }
            // 添加一个空行，分隔城市列表和路线信息
            writer.println();

            // 写入路线信息
            for (int i = 0; i < vertexCount; i++) {
                Edge edge = vertices[i].firstEdge;
                while (edge != null) {
                    writer.printf("%s,%s,%s,%s,%s,%s,%.1f\n",
                            vertices[i].name, vertices[edge.to].name,
                            edge.type, edge.flightNo, edge.departure,
                            edge.arrival, edge.cost);
                    edge = edge.next;
                }
            }
        } catch (IOException e) {
            System.err.println("保存文件失败:" + e.getMessage());
        }
    }

    // 删除路线
    public void deleteRoute(String from, String to) {
        if (!cityIndex.containsKey(from) || !cityIndex.containsKey(to)) {
            System.out.println("城市不存在！");
            return;
        }

        int fromIdx = cityIndex.get(from);
        int toIdx = cityIndex.get(to);

        // 先检查是否有直接相连的路线
        Edge current = vertices[fromIdx].firstEdge;
        List<Edge> directRoutes = new ArrayList<>();

        while (current != null) {
            if (current.to == toIdx) {
                directRoutes.add(current);
            }
            current = current.next;
        }

        if (directRoutes.isEmpty()) {
            System.out.println("这两个城市之间没有直接相连的路线！");
            return;
        }

        // 显示所有直接相连的路线
        System.out.println("\n从 " + from + " 到 " + to + " 的所有班次：");
        for (int i = 0; i < directRoutes.size(); i++) {
            Edge route = directRoutes.get(i);
            System.out.printf("%d. %s %s %s->%s 票价%.1f元\n",
                    i + 1,
                    route.type,
                    route.flightNo,
                    route.departure,
                    route.arrival,
                    route.cost);
        }

        // 获取要删除的班次信息
        Scanner sc = new Scanner(System.in);
        System.out.println("请输入要删除的班次编号和出发时间(格式:班次号,出发时间)，例如:K7731,08:00");
        String input = sc.nextLine();
        String[] parts = input.split(",");

        if (parts.length != 2) {
            System.out.println("输入格式错误！");
            return;
        }

        String targetFlightNo = parts[0].trim();
        String targetDeparture = parts[1].trim();

        // 删除指定的班次
        Edge prev = null; // 用于记录前一个 Edge 节点
        current = vertices[fromIdx].firstEdge; // 从起点顶点的第一个边开始遍历
        boolean found = false; // 标记是否找到了要删除的班次

        while (current != null) { // 遍历链表直到找到目标班次或到达链表末尾
            if (current.to == toIdx && // 检查目标顶点索引是否匹配
                    current.flightNo.equals(targetFlightNo) && // 检查航班号是否匹配
                    current.departure.equals(targetDeparture)) { // 检查出发时间是否匹配

                if (prev == null) { // 如果当前节点是第一个节点
                    vertices[fromIdx].firstEdge = current.next; // 更新起点顶点的第一个边为下一个边
                } else {
                    prev.next = current.next; // 将前一个节点的 next 指向当前节点的下一个节点
                }
                System.out.printf("已删除路线: %s -> %s (%s %s %s->%s)\n",
                        from, to, current.type, current.flightNo,
                        current.departure, current.arrival); // 打印已删除的班次信息
                found = true; // 设置找到标志为 true
                // 自动保存到文件
                saveToFile("src/com/yueqi/CurriculumDesign/routes.txt"); // 保存更新后的数据到文件
                break; // 结束循环
            }
            prev = current; // 更新前一个节点为当前节点
            current = current.next; // 移动到下一个节点
        }


        if (!found) {
            System.out.println("未找到指定班次的路线！");
        }
    }

    // 计算最优路径
    public void findOptimalRoute(String start, String end, String criterion) {
        // 检查起始城市和终点城市是否存在
        if (!cityIndex.containsKey(start) || !cityIndex.containsKey(end)) {
            System.out.println("城市不存在！");
            return;
        }

        // 获取起始城市和终点城市的索引
        int startIdx = cityIndex.get(start);
        int endIdx = cityIndex.get(end);

        // 初始化距离数组、前驱数组、到达时间数组、选中边数组和访问标记数组
        double[] dist = new double[vertexCount]; // 存储从起始城市到每个城市的最短距离
        int[] prev = new int[vertexCount];       // 存储每个城市的前驱城市
        int[] arrivalTimes = new int[vertexCount]; // 存储到达每个城市的到达时间（分钟）
        Edge[] selectedEdges = new Edge[vertexCount]; // 存储选中的边
        boolean[] visited = new boolean[vertexCount]; // 标记每个城市是否已访问

        // 初始化总费用和总时间
        double totalCost = 0;  // 记录总费用
        int totalTime = 0;     // 记录总时间（未使用）

        // 将距离数组初始化为无穷大，前驱数组初始化为-1，到达时间数组初始化为0
        Arrays.fill(dist, Double.MAX_VALUE);
        Arrays.fill(prev, -1);
        Arrays.fill(arrivalTimes, 0);
        dist[startIdx] = 0; // 起始城市的距离为0

        // 使用优先队列存储待处理的城市，按距离从小到大排序
        PriorityQueue<int[]> pq = new PriorityQueue<>((a, b) ->
                Double.compare(dist[a[0]], dist[b[0]]));
        pq.offer(new int[]{startIdx, 0}); // 将起始城市加入优先队列

        // Dijkstra算法的核心循环
        while (!pq.isEmpty()) {
            int[] current = pq.poll(); // 取出距离最小的城市
            int u = current[0];
            if (visited[u]) continue; // 如果已经访问过，过
            visited[u] = true; // 标记为已访问

            // 遍历当前城市的所有邻接边
            for (Edge edge = vertices[u].firstEdge; edge != null; edge = edge.next) {
                int v = edge.to; // 邻接城市的索引
                double weight;   // 边的权重

                // 根据决策原则计算权重
                if (criterion.equals("time")) {
                    // 对于起始站(u == startIdx)，不计算等待时间
                    int lastArrivalTime = 0;
                    if (prev[u] != -1) {
                        Edge lastEdge = selectedEdges[u];
                        lastArrivalTime = lastEdge.timeToMinutes(lastEdge.arrival); // 上一条边的到达时间
                    }
                    weight = edge.getTotalDuration(lastArrivalTime, u == startIdx); // 计算总耗时
                } else {
                    weight = edge.cost; // 费用作为权重
                }

                // 如果通过当前城市到达邻接城市更短，则更新距离、前驱、选中边和到达时间
                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    prev[v] = u;
                    selectedEdges[v] = edge;
                    if (criterion.equals("time")) {
                        // 更新到达时间
                        arrivalTimes[v] = (edge.timeToMinutes(edge.arrival)) % (24 * 60); // 到达时间（分钟）
                    }
                    pq.offer(new int[]{v, 0}); // 将邻接城市加入优先队列
                }
            }
        }

        // 如果终点城市的距离仍为无穷大，说明无法到达
        if (dist[endIdx] == Double.MAX_VALUE) {
            System.out.println("无法到达目的地！");
        } else {
            System.out.println("\n最优路径信息:");
            // 计算实际耗时
            int actualTime = 0;
            totalCost = 0;
            int current = endIdx;
            while (prev[current] != -1) {
                Edge edge = selectedEdges[current];
                totalCost += edge.cost; // 累加费用
                actualTime += edge.calculateDuration();  // 计算实际行程时间

                // 如果不是起始站，计算等待时间
                if (prev[prev[current]] != -1) {
                    Edge prevEdge = selectedEdges[prev[current]];
                    int lastArrivalTime = prevEdge.timeToMinutes(prevEdge.arrival); // 上一条边的到达时间
                    int departureTime = edge.timeToMinutes(edge.departure); // 当前边的出发时间

                    // 计算等待时间
                    if (departureTime < lastArrivalTime) {
                        // 如果出发时间小于到达时间，说明跨天了
                        actualTime += (24 * 60 - lastArrivalTime) + departureTime;
                    } else {
                        actualTime += departureTime - lastArrivalTime;
                    }
                }
                current = prev[current];
            }

            // 打印详细路径
            printDetailedPath(prev, selectedEdges, arrivalTimes, startIdx, endIdx);

            // 显示总时间和费用
            if (criterion.equals("time")) {
                int days = actualTime / (24 * 60); // 天数
                int hours = (actualTime % (24 * 60)) / 60; // 小时
                int minutes = actualTime % 60; // 分钟
                System.out.printf("总时间: %d天%d小时%d分钟\n", days, hours, minutes);
                System.out.printf("总费用: %.1f元\n", totalCost);
            } else {
                System.out.printf("总费用: %.1f元\n", dist[endIdx]);
                // 同样显示时间
                int days = actualTime / (24 * 60); // 天数
                int hours = (actualTime % (24 * 60)) / 60; // 小时
                int minutes = actualTime % 60; // 分钟
                System.out.printf("总时间: %d天%d小时%d分钟\n", days, hours, minutes);
            }
        }
    }

    // 添加详细路径打印方法
    private void printDetailedPath(int[] prev, Edge[] selectedEdges, int[] arrivalTimes,
                                   int startIdx, int endIdx) {
        if (prev[endIdx] != -1) {
            printDetailedPath(prev, selectedEdges, arrivalTimes, startIdx, prev[endIdx]);
            Edge edge = selectedEdges[endIdx];

            // 只有对于中转站才显示等待时间
            if (prev[prev[endIdx]] != -1) {
                Edge prevEdge = selectedEdges[prev[endIdx]];
                int lastArrivalTime = prevEdge.timeToMinutes(prevEdge.arrival);
                int departureTime = edge.timeToMinutes(edge.departure);
                int waitTime;

                if (departureTime < lastArrivalTime) {
                    // 如果出发时间小于到达时间，说明跨天了
                    waitTime = (24 * 60 - lastArrivalTime) + departureTime;
                } else {
                    waitTime = departureTime - lastArrivalTime;
                }

                // 格式化等待时间显示
                String waitTimeStr;
                if (waitTime < 60) {
                    waitTimeStr = String.format("%d分钟", waitTime);
                } else {
                    int hours = waitTime / 60;
                    int minutes = waitTime % 60;
                    if (minutes == 0) {
                        waitTimeStr = String.format("%d小时", hours);
                    } else {
                        waitTimeStr = String.format("%d小时%d分钟", hours, minutes);
                    }
                }

                System.out.printf("%s -> %s: %s%s %s->%s (等待%s)\n",
                        vertices[prev[endIdx]].name,
                        vertices[endIdx].name,
                        edge.type,
                        edge.flightNo.isEmpty() ? "" : "(" + edge.flightNo + ")",
                        edge.departure,
                        edge.arrival,
                        waitTimeStr);
            } else {
                // 起始站不显示等待时间
                System.out.printf("%s -> %s: %s%s %s->%s\n",
                        vertices[prev[endIdx]].name,
                        vertices[endIdx].name,
                        edge.type,
                        edge.flightNo.isEmpty() ? "" : "(" + edge.flightNo + ")",
                        edge.departure,
                        edge.arrival);
            }
        }
    }

    // 查找指定两站之间的班次
    public void findDirectRoutes(String from, String to) {
        if (!cityIndex.containsKey(from) || !cityIndex.containsKey(to)) {
            System.out.println("城市不存在！");
            return;
        }

        int fromIdx = cityIndex.get(from);
        int toIdx = cityIndex.get(to);

        // 查找直接相连的路线
        Edge current = vertices[fromIdx].firstEdge;
        List<Edge> directRoutes = new ArrayList<>();

        while (current != null) {
            if (current.to == toIdx) {
                directRoutes.add(current);
            }
            current = current.next;
        }

        if (directRoutes.isEmpty()) {
            System.out.println("这两个城市之间没有直接相连的路线！");
            return;
        }

        // 显示所有直接相连的路线
        System.out.println("\n从 " + from + " 到 " + to + " 的所有班次：");
        for (int i = 0; i < directRoutes.size(); i++) {
            Edge route = directRoutes.get(i);
            System.out.printf("%d. %s %s %s->%s 票价%.1f元\n",
                    i + 1,
                    route.type,
                    route.flightNo,
                    route.departure,
                    route.arrival,
                    route.cost);
        }
    }

    // 删除城市
    public void deleteCity(String cityName) {
        // 检查城市是否存在
        if (!cityIndex.containsKey(cityName)) {
            System.out.println("城市不存在！");
            return;
        }

        Scanner sc = new Scanner(System.in);
        System.out.println("\n!!! 警告 !!!");
        System.out.println("您正在尝试删除城市: " + cityName);
        System.out.println("此操作将同时删除所有与该城市相关的路线！");
        System.out.println("此操作不可撤销！");
        System.out.print("如果确定要删除，请输入城市名称进行确认: ");

        String confirmation = sc.nextLine().trim();
        // 确认用户输入是否匹配城市名称
        if (!confirmation.equals(cityName)) {
            System.out.println("输入的城市名称不匹配，已取消删除操作。");
            return;
        }

        int targetIdx = cityIndex.get(cityName); // 获取目标城市的索引

        // 删除所有以此城市为终点的路线
        for (int i = 0; i < vertexCount; i++) {
            if (i == targetIdx) continue; // 跳过目标城市本身

            Edge prev = null;
            Edge current = vertices[i].firstEdge;

            while (current != null) {
                if (current.to == targetIdx) { // 如果当前边的目标是目标城市
                    if (prev == null) {
                        vertices[i].firstEdge = current.next; // 更新起点顶点的第一个边为下一个边
                    } else {
                        prev.next = current.next; // 将前一个节点的 next 指向当前节点的下一个节点
                    }
                    current = current.next; // 继续检查下一个边
                } else {
                    prev = current;
                    current = current.next;
                }
            }
        }

        // 删除以此城市为起点的所有路线
        vertices[targetIdx].firstEdge = null;

        // 更新城市索引和顶点数组
        cityIndex.remove(cityName); // 从城市索引中移除目标城市
        for (String city : cityIndex.keySet()) {
            int idx = cityIndex.get(city);
            if (idx > targetIdx) {
                cityIndex.put(city, idx - 1); // 更新索引，使后续城市的索引减一
            }
        }

        // 移动顶点数组，填补被删除城市的位置
        for (int i = targetIdx; i < vertexCount - 1; i++) {
            vertices[i] = vertices[i + 1];
            // 更新所有边的目标索引
            Edge edge = vertices[i].firstEdge;
            while (edge != null) {
                if (edge.to > targetIdx) {
                    edge.to--; // 减少目标索引，以保持一致性
                }
                edge = edge.next;
            }
        }
        vertexCount--; // 减少顶点计数

        System.out.println("城市 " + cityName + " 及其相关路线已成功删除！");
        // 自动保存到文件
        saveToFile("src/com/yueqi/CurriculumDesign/routes.txt");
    }

    public static void main(String[] args) {
        NationalTrafficAdvisorySimulation system = new NationalTrafficAdvisorySimulation(100);
        Scanner sc = new Scanner(System.in);
        String defaultFile = "src/com/yueqi/CurriculumDesign/routes.txt";

        // 加载数据文件
        File file = new File(defaultFile);
        if (file.exists()) {
            system.loadFromFile(defaultFile);
            System.out.println("已加载路线数据");
        } else {
            System.out.println("未找到路线数据文件");
        }

        while (true) {
            System.out.println("\n=== 全国交通咨询系统 ===");
            System.out.println("1. 显示所有路线");
            System.out.println("2. 查询最优路径");
            System.out.println("3. 查询两站间班次");
            System.out.println("4. 添加新路线");
            System.out.println("5. 删除路线");
            System.out.println("6. 添加新城市");
            System.out.println("7. 删除城市");
            System.out.println("8. 退出系统");
            System.out.print("请选择操作(1-8):");

            int choice = sc.nextInt();
            sc.nextLine(); // 消耗换行符

            switch (choice) {
                case 1:
                    system.printAllRoutes();
                    break;

                case 2:
                    System.out.println("请输入起始站和终点站(格式:起点,终点):");
                    String[] route = sc.nextLine().split(",");
                    if (route.length == 2) {
                        System.out.println("请选择决策原则(time/cost):");
                        String criterion = sc.nextLine();
                        system.findOptimalRoute(route[0], route[1], criterion);
                    }
                    break;

                case 3:
                    System.out.println("请输入要查询的两个站点(格式:起点,终点):");
                    String[] stations = sc.nextLine().split(",");
                    if (stations.length == 2) {
                        system.findDirectRoutes(stations[0], stations[1]);
                    }
                    break;

                case 4:
                    System.out.println("请输入路线信息(格式:起点,终点,类型,班次号,出发时间,到达时间,票价):");
                    String[] parts = sc.nextLine().split(",");
                    if (parts.length == 7) {
                        system.addRoute(parts[0], parts[1], parts[2], parts[3],
                                parts[4], parts[5], Double.parseDouble(parts[6]));
                        System.out.println("添加成功！");
                    }
                    break;

                case 5:
                    System.out.println("请输入要删除的路线(格式:起点,终点):");
                    String[] cities = sc.nextLine().split(",");
                    if (cities.length == 2) {
                        system.deleteRoute(cities[0], cities[1]);
                    }
                    break;

                case 6:
                    System.out.println("请输入新城市名称:");
                    String newCity = sc.nextLine();
                    system.addCity(newCity);
                    System.out.println("添加成功！");
                    break;

                case 7:
                    System.out.println("请输入要删除的城市名称:");
                    String cityToDelete = sc.nextLine();
                    system.deleteCity(cityToDelete);
                    break;

                case 8:
                    System.out.println("感谢使用！");
                    return;

                default:
                    System.out.println("无效的选择！");
            }
        }
    }
}
/*
西安
西安,重庆,火车,K619,07:10,17:37,98.0
西安,重庆,火车,G1833,13:16,18:54,416.0
西安,重庆,火车,G1835,17:10,22:27,409.0
郑州,西安,火车,Z293,00:34,06:34,72.0
郑州,西安,火车,G2201,07:11,09:31,239.0
郑州,西安,火车,G361,12:48,15:07,239.0
郑州,西安,火车,G843,18:24,20:14,221.0
重庆,西安,火车,D2001,07:11,12:42,282.5
重庆,西安,火车,G3210,11:37,17:46,416.0
重庆,西安,火车,D1989,18:27,23:36,279.5
西安,郑州,火车,Z362,00:21,06:12,72.0
西安,郑州,火车,G1914,06:20,08:22,239.0
西安,郑州,火车,G430,12:00,14:08,239.0
西安,郑州,火车,K420,18:30,01:48,72.0
 */