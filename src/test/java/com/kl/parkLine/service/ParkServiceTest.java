package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.enums.Gender;

@SpringBootTest
public class ParkServiceTest
{
    @Autowired
    private ParkService parkService;
     
    @Test
    @Transactional
    public void testCalAmt()
    {
        Park park = parkService.findOneByCode("parkCode01");
        String json = JSON.toJSONString(park);
        assertNotEquals(json.length(), 0);
    }
    
    @Test
    public void testGender()
    {
        Gender gender = Gender.valueOf("male");
        assertEquals(gender, Gender.male);
    }
}
