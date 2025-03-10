//package com.yueqi.ntas.service;
//
//import com.yueqi.ntas.service.impl.CityServiceImpl;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//
//import java.util.Arrays;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//@SpringBootTest
//class CityServiceTest {
//
//    @Autowired
//    private CityService cityService;
//
//    @MockBean
//    private GraphService graphService;
//
//    @Test
//    void testAddCity() {
//        String cityName = "西安";
//        cityService.addCity(cityName);
//        verify(graphService).addVertex(cityName);
//    }
//
//    @Test
//    void testAddCityCQ() {
//        String cityName = "重庆";
//        cityService.addCity(cityName);
//        verify(graphService).addVertex(cityName);
//    }
//
//    @Test
//    void testDeleteCity() {
//        String cityName = "西安";
//        cityService.deleteCity(cityName);
//        verify(graphService).removeVertex(cityName);
//    }
//
//    @Test
//    void testGetAllCities() {
//        List<String> expectedCities = Arrays.asList("西安", "北京", "上海");
//        when(cityService.getAllCities()).thenReturn(expectedCities);
//
//        List<String> actualCities = cityService.getAllCities();
//        assertEquals(expectedCities, actualCities);
//    }
//
//    @Test
//    void testFindAdjacentCities() {
//        String fromCity = "西安";
//        List<String> expectedAdjacent = Arrays.asList("北京", "上海");
//        when(cityService.findAdjacentCities(fromCity)).thenReturn(expectedAdjacent);
//
//        List<String> actualAdjacent = cityService.findAdjacentCities(fromCity);
//        assertEquals(expectedAdjacent, actualAdjacent);
//    }
//}