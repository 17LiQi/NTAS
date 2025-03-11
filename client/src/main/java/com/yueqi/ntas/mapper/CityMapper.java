package com.yueqi.ntas.mapper;

import com.yueqi.ntas.entity.City;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CityMapper {
    @Select("SELECT * FROM city ORDER BY convert(name using gbk) COLLATE gbk_chinese_ci")
    List<City> findAll();
    
    @Select("SELECT * FROM city WHERE name = #{name}")
    City findByName(String name);
    
    @Insert("INSERT INTO city(name) VALUES(#{name})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(City city);
    
    @Delete("DELETE FROM city WHERE name = #{name}")
    int deleteByName(String name);
    
    @Select("SELECT * FROM city WHERE id = #{id}")
    City findById(Integer id);
    
    @Select("SELECT * FROM city WHERE name LIKE CONCAT(#{query}, '%') ORDER BY convert(name using gbk) COLLATE gbk_chinese_ci")
    List<City> findByNameLike(String query);
} 