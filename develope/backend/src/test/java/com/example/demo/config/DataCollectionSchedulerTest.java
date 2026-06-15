package com.example.demo.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DataCollectionSchedulerTest {
    @Autowired
    public DataCollectionScheduler dataCollectionScheduler;
    @Test
    public void t1(){
        dataCollectionScheduler.dailyCollection();
    }
}