package com.kl.parkLine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.boyue.BoyueRespWrap;
import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.service.OrderService;

@SpringBootTest
@AutoConfigureMockMvc
public class BoyueControllerTest
{
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private MockMvc mockMvc;
     
    /**
     * 简单出入场
     * @throws Exception 
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testSimpleInOut() throws Exception
    {
        String path = "/boyue/plateNotify";
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        is.close();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventIn))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String retContent = result.getResponse().getContentAsString();
        
        //开闸入场
        BoyueRespWrap boyueRespWrap = (BoyueRespWrap) JSONObject.parse(retContent);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
    }
    
}
