package com.yueqi.ntas.service;

import java.util.List;

public interface CityService {
    void addCity(String cityName);
    void deleteCity(String cityName);
    boolean exists(String cityName);
    List<String> getAllCities();
    List<String> findAdjacentCities(String fromCity);
    List<String> getCities(String query);
} 