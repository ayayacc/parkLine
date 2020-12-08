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
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsLoginParam;
import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.ParkLocationVo;
import com.kl.parkLine.vo.SmsCodeVo;

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
     * 短信登录
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    private String login() throws Exception 
    {
        //获取短信验证码
        JSONObject getSmsCode = new JSONObject();
        String mobile = "13807721234";
        getSmsCode.put("mobile", mobile);
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/sms/getCode")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(getSmsCode))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String retContent = mvcResult.getResponse().getContentAsString();
        RestResult<SmsCodeVo> smsCodeResult = (RestResult<SmsCodeVo>) JSONObject.parse(retContent);
        assertEquals(Const.RET_OK, smsCodeResult.getRetCode());
        
        //登录
        SmsLoginParam smsLoginParam = new SmsLoginParam();
        smsLoginParam.setMobile(mobile);
        smsLoginParam.setValidCode(smsCodeResult.getData().getCode());
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/sms/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(getSmsCode))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = mvcResult.getResponse().getContentAsString();
        RestResult<JwtToken> jwtTokenResult = (RestResult<JwtToken>) JSONObject.parse(retContent);
        assertEquals(Const.RET_OK, jwtTokenResult.getRetCode());
        return jwtTokenResult.getData().getToken();
    }
    
    /**
     * 简单出入场
     * @throws Exception 
     */
    @SuppressWarnings("unchecked")
    @Test
    @Transactional
    @Rollback(true)
    public void testSimpleInOut() throws Exception
    {
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        is.close();
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventIn))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String retContent = result.getResponse().getContentAsString();
        
        //开闸入场
        BoyueRespWrap boyueRespWrap = (BoyueRespWrap) JSONObject.parse(retContent);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        String token = login();
        //反向寻车
        result = mockMvc.perform(MockMvcRequestBuilders.get("/getParkLocation")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(boyueEventIn))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        RestResult<ParkLocationVo> parkLocationResult = (RestResult<ParkLocationVo>) JSONObject.parse(retContent);
        assertEquals("FixedPark01", parkLocationResult.getData().getName());
    }
    
}
