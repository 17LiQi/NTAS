# 全国交通查询模拟-NationalTrafficAdvisorySimulation-NTAS
## 基本介绍
- 该项目基于mysql数据库,springboot后端以及vue前端编写的模拟全国交通管理系统
- 项目基本功能:
- 
  1.显示全部交通路线
  
  2.查询最优路线(决策方案可选时间最短与路径最短)
  
  3.查询相邻站点
  
  4.添加/删除路线
  
  5.添加/删除城市
  
- 逻辑原理:java数据结构图与邻接表
## 特点
- 拥有齐全的项目开发流程(数据库->后端->前端)
- 包含完整的pring Boot三层架构:控制层(Controller)业务层(Service)持久层(Repository/DAO)
- 利用MyBatis Puls简化代码
## 尚存问题:
- 所有路线和城市均为手动添加
- 后端代码依旧不够清晰简洁易读
- 前端代码比较臃肿
- 内存管理逻辑还未优化
***
# National Traffic Advisory Simulation (NTAS)

## Basic Introduction
- This project is a simulated national traffic management system built with a MySQL database, Spring Boot backend, and Vue frontend.
- Basic Features of the Project:
  1. Display all traffic routes.
  2. Query the optimal route (with decision options for shortest time or shortest path).
  3. Query adjacent stations.
  4. Add/remove routes.
  5. Add/remove cities.
- Logical Principle: Java data structure graphs and adjacency lists.

## Features
- Complete project development process (Database -> Backend -> Frontend).
- Full Spring Boot three-tier architecture: Controller (Control Layer), Service (Business Layer), and Repository/DAO (Persistence Layer).
- Simplified code with MyBatis Plus.

## Existing Issues:
- All routes and cities are manually added.
- The backend code is still not clear, concise, and readable enough.
- The frontend code is relatively bloated.
- Memory management logic has not been optimized yet.
