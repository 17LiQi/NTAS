-- 创建数据库
CREATE DATABASE IF NOT EXISTS ntas DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ntas;

-- 创建城市表
CREATE TABLE IF NOT EXISTS city (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建路线表
CREATE TABLE IF NOT EXISTS route (
    id INT PRIMARY KEY AUTO_INCREMENT,
    from_city_id INT NOT NULL,
    to_city_id INT NOT NULL,
    transport_type VARCHAR(20) NOT NULL COMMENT '交通类型:火车,飞机',
    route_no VARCHAR(20) NOT NULL COMMENT '路线编号',
    departure TIME NOT NULL COMMENT '出发时间',
    arrival TIME NOT NULL COMMENT '到达时间',
    fare DECIMAL(10,2) NOT NULL COMMENT '票价',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (from_city_id) REFERENCES city(id),
    FOREIGN KEY (to_city_id) REFERENCES city(id),
    UNIQUE KEY uk_route (from_city_id, to_city_id, route_no, departure)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入城市数据
INSERT INTO city (name) VALUES 
('北京'),('上海'),('广州'),('天津'),('郑州'),
('株洲'),('南昌'),('重庆'),('贵阳'),('兰州'),
('西宁'),('西安');

-- 插入路线数据
INSERT INTO route (from_city_id, to_city_id, transport_type, route_no, departure, arrival, fare) VALUES
-- 北京出发的路线
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'G51', '06:55', '13:55', 911.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'D49', '11:10', '09:35', 911.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'D95', '17:53', '11:27', 298.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '天津'), '火车', 'K7731', '00:21', '02:05', 19.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '天津'), '火车', 'C2557', '06:46', '07:07', 38.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '天津'), '火车', 'C2583', '16:02', '16:23', 38.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '天津'), '火车', 'C2583', '16:02', '16:23', 38.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '天津'), '火车', 'Z159', '22:29', '23:53', 23.5),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G559', '06:15', '09:05', 309.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'D49', '11:08', '17:09', 121.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G507', '16:30', '20:00', 309.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'D29', '07:05', '10:25', 309.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'G429', '12:40', '21:38', 700.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'Z179', '18:26', '14:38', 213.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '兰州'), '飞机', 'HU7197', '07:55', '10:25', 1060.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '兰州'), '飞机', 'MU8110', '12:50', '16:35', 567.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '兰州'), '飞机', 'MU2416', '19:10', '21:40', 492.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '贵阳'), '飞机', 'NS8077', '09:40', '14:50', 350.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '贵阳'), '飞机', 'CA4166', '21:00', '00:35', 830.0),
((SELECT id FROM city WHERE name = '北京'), (SELECT id FROM city WHERE name = '贵阳'), '飞机', 'GY7102', '06:30', '10:10', 650.0),

-- 上海出发的路线
((SELECT id FROM city WHERE name = '上海'), (SELECT id FROM city WHERE name = '天津'), '飞机', 'MU8335', '07:30', '10:00', 600.0),
((SELECT id FROM city WHERE name = '上海'), (SELECT id FROM city WHERE name = '天津'), '飞机', 'KN2902', '21:55', '00:10', 400.0),
((SELECT id FROM city WHERE name = '上海'), (SELECT id FROM city WHERE name = '天津'), '飞机', 'KN2906', '12:50', '15:20', 300.0),
((SELECT id FROM city WHERE name = '上海'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'G1321', '06:10', '10:10', 337.0),
((SELECT id FROM city WHERE name = '上海'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'G1352', '13:24', '17:15', 337.0),
((SELECT id FROM city WHERE name = '上海'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'G1395', '18:28', '22:19', 337.0),

-- 广州出发的路线
((SELECT id FROM city WHERE name = '广州'), (SELECT id FROM city WHERE name = '重庆'), '飞机', 'HU7141', '09:00', '11:15', 690.0),
((SELECT id FROM city WHERE name = '广州'), (SELECT id FROM city WHERE name = '重庆'), '飞机', 'CZ3439', '14:35', '16:50', 449.0),
((SELECT id FROM city WHERE name = '广州'), (SELECT id FROM city WHERE name = '重庆'), '飞机', 'PN6206', '23:40', '00:55', 335.0),
((SELECT id FROM city WHERE name = '广州'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'D8102', '06:36', '11:43', 265.0),
((SELECT id FROM city WHERE name = '广州'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'G320', '12:56', '16:44', 364.0),
((SELECT id FROM city WHERE name = '广州'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'D1868', '18:04', '23:13', 291.0),

-- 天津出发的路线
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '上海'), '飞机', 'KN2905', '07:00', '10:00', 300.0),
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '上海'), '飞机', 'MU5144', '14:55', '17:00', 850.0),
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '上海'), '飞机', 'MU5644', '21:25', '23:15', 350.0),
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '北京'), '火车', 'Z160', '03:18', '04:52', 23.5),
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '北京'), '火车', 'G1596', '11:12', '11:52', 53.0),
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '北京'), '火车', 'C2626', '16:25', '16:51', 44.5),
((SELECT id FROM city WHERE name = '天津'), (SELECT id FROM city WHERE name = '北京'), '火车', 'G194', '21:04', '21:38', 56.0),

-- 郑州出发的路线
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'Z167', '00:19', '09:11', 124.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'G2821', '12:26', '16:36', 419.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'T145', '19:29', '05:20', 124.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'K132', '06:43', '20:13', 124.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'G639', '14:03', '18:57', 382.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'T167', '21:25', '07:27', 115.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '北京'), '火车', 'Z502', '00:46', '06:51', 93.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '北京'), '火车', 'G1556', '08:53', '12:19', 309.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '北京'), '火车', 'G526', '18:42', '22:43', 320.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '西安'), '火车', 'Z293', '00:34', '06:34', 72.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '西安'), '火车', 'G2201', '07:11', '09:31', 239.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '西安'), '火车', 'G361', '12:48', '15:07', 239.0),
((SELECT id FROM city WHERE name = '郑州'), (SELECT id FROM city WHERE name = '西安'), '火车', 'G843', '18:24', '20:14', 221.0),

-- 株洲出发的路线
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'T172', '02:36', '06:41', 53.5),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'G2340', '11:42', '13:38', 181.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '南昌'), '火车', 'T152', '18:34', '20:24', 181.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G506', '08:25', '12:44', 415.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'Z138', '15:28', '01:15', 124.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G440', '18:42', '22:40', 415.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'Z287', '06:23', '19:26', 115.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'K137', '11:25', '02:57', 115.0),
((SELECT id FROM city WHERE name = '株洲'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'K875', '20:09', '12:07', 115.0),

-- 南昌出发的路线
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'K30', '01:44', '15:10', 124.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G3156', '14:43', '19:52', 366.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'K134', '22:58', '12:58', 124.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '上海'), '火车', 'G1382', '07:20', '11:16', 342.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '上海'), '火车', 'G1338', '19:15', '22:59', 322.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '上海'), '火车', 'K121', '23:07', '10:36', 105.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'G631', '07:55', '10:02', 197.0),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'T171', '18:22', '22:46', 53.5),
((SELECT id FROM city WHERE name = '南昌'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'T151', '23:01', '03:02', 53.5),

-- 重庆出发的路线
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'D6175', '06:09', '08:15', 138.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'G3723', '10:04', '12:13', 139.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'G2887', '16:43', '18:48', 139.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'D2003', '21:22', '23:39', 111.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '广州'), '飞机', 'AQ1102', '23:10', '01:20', 300.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '广州'), '飞机', 'CA4361', '07:00', '08:50', 300.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '广州'), '飞机', 'AQ1102', '17:55', '19:55', 259.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '北京'), '火车', 'G52', '07:30', '14:31', 887.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '北京'), '火车', 'G372', '12:34', '21:39', 825.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '北京'), '火车', 'D4', '16:07', '10:37', 298.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '西安'), '火车', 'D2001', '07:11', '12:42', 282.5),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '西安'), '火车', 'G3210', '11:37', '17:46', 416.0),
((SELECT id FROM city WHERE name = '重庆'), (SELECT id FROM city WHERE name = '西安'), '火车', 'D1989', '18:27', '23:36', 279.5),

-- 贵阳出发的路线
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '广州'), '火车', 'G3733', '08:08', '12:05', 368.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '广州'), '火车', 'D1809', '14:04', '19:23', 317.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '广州'), '火车', 'G2925', '18:52', '22:45', 370.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'K986', '05:11', '19:30', 152.5),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'D208', '08:12', '19:06', 338.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'K80', '02:52', '16:10', 115.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'K112', '09:08', '22:47', 115.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '株洲'), '火车', 'K492', '21:09', '10:44', 115.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '北京'), '飞机', 'CA4161', '07:30', '10:20', 520.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '北京'), '飞机', 'NS8078', '15:50', '21:00', 330.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '北京'), '飞机', 'CA4165', '18:55', '21:40', 330.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'K142', '04:52', '08:36', 97.5),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'G3476', '09:31', '12:19', 151.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'D1826', '16:07', '18:49', 145.0),
((SELECT id FROM city WHERE name = '贵阳'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'D1840', '20:38', '22:03', 138.0),

-- 兰州出发的路线
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '北京'), '飞机', 'MU2411', '07:40', '09:55', 630.0),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '北京'), '飞机', 'MU8111', '17:25', '20:50', 490.0),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '北京'), '飞机', 'MU8211', '20:20', '22:40', 310.0),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '西宁'), '火车', 'K1059', '04:16', '07:05', 32.5),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '西宁'), '火车', 'Z223', '08:40', '11:22', 55.0),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '西宁'), '火车', 'T197', '14:26', '16:04', 28.5),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '西宁'), '火车', 'D165', '21:06', '23:25', 29.5),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'D206', '11:36', '22:11', 338.0),
((SELECT id FROM city WHERE name = '兰州'), (SELECT id FROM city WHERE name = '贵阳'), '火车', 'K988', '23:56', '14:34', 152.5),

-- 西宁出发的路线
((SELECT id FROM city WHERE name = '西宁'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'K570', '04:13', '06:22', 29.5),
((SELECT id FROM city WHERE name = '西宁'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'D166', '08:05', '09:56', 55.0),
((SELECT id FROM city WHERE name = '西宁'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'D2674', '16:25', '17:53', 71.5),
((SELECT id FROM city WHERE name = '西宁'), (SELECT id FROM city WHERE name = '兰州'), '火车', 'K988', '20:35', '23:27', 32.5),

-- 西安出发的路线
((SELECT id FROM city WHERE name = '西安'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'G1833', '13:16', '18:54', 416.0),
((SELECT id FROM city WHERE name = '西安'), (SELECT id FROM city WHERE name = '重庆'), '火车', 'G1835', '17:10', '22:27', 409.0),
((SELECT id FROM city WHERE name = '西安'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'Z362', '00:21', '06:12', 72.0),
((SELECT id FROM city WHERE name = '西安'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G1914', '06:20', '08:22', 239.0),
((SELECT id FROM city WHERE name = '西安'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'G430', '12:00', '14:08', 239.0),
((SELECT id FROM city WHERE name = '西安'), (SELECT id FROM city WHERE name = '郑州'), '火车', 'K420', '18:30', '01:48', 72.0);