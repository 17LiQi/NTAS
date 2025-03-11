//package com.yueqi.ntas.service;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//class FileServiceTest {
//
//    @Autowired
//    private FileService fileService;
//
//    @Test
//    void testLoadAndSaveFile() {
//        String filename = "test-routes.txt";
//        assertDoesNotThrow(() -> fileService.loadFromFile(filename));
//        assertDoesNotThrow(() -> fileService.saveToFile());
//    }
//}