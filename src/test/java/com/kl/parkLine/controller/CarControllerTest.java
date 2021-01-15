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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kl.parkLine.enums.RetCode;
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsCheckParam;
import com.kl.parkLine.vo.SmsCodeVo;

@SpringBootTest
@AutoConfigureMockMvc
public class CarControllerTest
{
     
    @Autowired
    private MockMvc mockMvc;
    
    /**
     * 短信登录
     * @throws Exception 
     */
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
        RestResult<SmsCodeVo> smsCodeResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<SmsCodeVo>>(){});
        assertEquals(RetCode.ok, smsCodeResult.getRetCode());
        
        //登录
        SmsCheckParam smsLoginParam = new SmsCheckParam();
        smsLoginParam.setMobile(mobile);
        smsLoginParam.setValidCode(smsCodeResult.getData().getCode());
        mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/sms/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(smsLoginParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = mvcResult.getResponse().getContentAsString();
        RestResult<JwtToken> jwtTokenResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<JwtToken>>(){});
        assertEquals(RetCode.ok, jwtTokenResult.getRetCode());
        return jwtTokenResult.getData().getToken();
    }
    
    /**
     * 非提前支付,停车1.5小时，无月票，钱包手动支付
     * 测试前数据:13807721234,钱包余额有足够余额
     * @throws Exception 
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testLock() throws Exception
    {
        //登录
        String token = login();
        //上传行驶证
        Resource resource = new ClassPathResource("/testData/drivingLicense/wrong.png");
        InputStream is = resource.getInputStream();
        MockMultipartFile mfile = new MockMultipartFile("licenseImg", is);
        mockMvc.perform(MockMvcRequestBuilders.multipart("/cars/lock/4")
                .file(mfile).header("Authorization", token)
                )
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andReturn();
        
        //String retContent = result.getResponse().getContentAsString();
        //RestResult<DrivingLicenseVo> vo = JSONObject.parseObject(retContent, RestResult<DrivingLicenseVo>.class);
    }
}
