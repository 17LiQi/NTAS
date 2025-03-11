package com.yueqi.ntas.mapper;

import com.yueqi.ntas.entity.Route;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface RouteMapper {
    @Results(id = "routeMap", value = {
        @Result(property = "fromCityId", column = "from_city_id"),
        @Result(property = "toCityId", column = "to_city_id"),
        @Result(property = "transportType", column = "transport_type"),
        @Result(property = "routeNo", column = "route_no"),
        @Result(property = "createTime", column = "create_time"),
        @Result(property = "updateTime", column = "update_time")
    })
    @Select("SELECT * FROM route ORDER BY route_no, departure, arrival")
    List<Route> findAll();
    
    @ResultMap("routeMap")
    @Select("SELECT * FROM route WHERE from_city_id = #{fromCityId} ORDER BY route_no, departure, arrival")
    List<Route> findByFromCity(Integer fromCityId);
    
    @ResultMap("routeMap")
    @Select("SELECT * FROM route WHERE from_city_id = #{fromCityId} AND to_city_id = #{toCityId} " +
           "ORDER BY route_no DESC, departure, arrival")
    List<Route> findByFromAndToCity(Integer fromCityId, Integer toCityId);
    
    @Insert("INSERT INTO route(from_city_id, to_city_id, transport_type, route_no, departure, arrival, fare) " +
            "VALUES(#{fromCityId}, #{toCityId}, #{transportType}, #{routeNo}, #{departure}, #{arrival}, #{fare})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Route route);
    
    @Delete("DELETE FROM route WHERE from_city_id = #{fromCityId} AND to_city_id = #{toCityId} " +
            "AND route_no = #{routeNo} AND departure = #{departure}")
    int delete(Route route);

    @ResultMap("routeMap")
    @Select("SELECT * FROM route WHERE from_city_id = (SELECT id FROM city WHERE name = #{fromCity}) ORDER BY route_no")
    List<Route> findByFromCityName(String fromCity);

    @ResultMap("routeMap")
    @Select("SELECT * FROM route WHERE to_city_id = #{toCityId} ORDER BY route_no")
    List<Route> findByToCity(Integer toCityId);

    @ResultMap("routeMap")
    @Select({
        "<script>",
        "SELECT * FROM route WHERE from_city_id = #{fromCityId} AND to_city_id = #{toCityId}",
        "<if test='criterion == \"cost\"'>ORDER BY fare</if>",
        "<if test='criterion == \"time\"'>ORDER BY TIMEDIFF(arrival, departure)</if>",
        "<if test='criterion == null or criterion == \"\"'>ORDER BY route_no</if>",
        "</script>"
    })
    List<Route> findOptimalRoute(Integer fromCityId, Integer toCityId, String criterion);

    @Select("SELECT COUNT(*) FROM route WHERE from_city_id = #{fromCityId} " +
            "AND to_city_id = #{toCityId} AND route_no = #{routeNo} AND departure = #{departure}")
    int checkExists(@Param("fromCityId") Integer fromCityId, 
                   @Param("toCityId") Integer toCityId,
                   @Param("routeNo") String routeNo,
                   @Param("departure") String departure);
} 