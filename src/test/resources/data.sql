-- 插入基础测试数据
INSERT INTO city (name) VALUES 
('西安'),('北京'),('上海'),('广州');

-- 插入基础路线数据
INSERT INTO route (from_city_id, to_city_id, transport_type, route_no, departure, arrival, fare) VALUES
(1, 2, '火车', 'K123', '08:00', '20:00', 100.0),
(1, 3, '火车', 'K125', '10:00', '18:00', 300.0); 