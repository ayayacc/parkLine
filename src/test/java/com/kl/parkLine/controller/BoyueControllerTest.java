package com.kl.parkLine.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStream;
import java.math.BigDecimal;

import org.joda.time.DateTime;
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
import com.alibaba.fastjson.TypeReference;
import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.boyue.BoyueRespWrap;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsLoginParam;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.service.DeviceService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.service.ParkService;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.OrderVo;
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
    private DeviceService deviceService;
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private ParkService parkService;
    
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
        assertEquals(Const.RET_OK, smsCodeResult.getRetCode());
        
        //登录
        SmsLoginParam smsLoginParam = new SmsLoginParam();
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
        assertEquals(Const.RET_OK, jwtTokenResult.getRetCode());
        return jwtTokenResult.getData().getToken();
    }
    
    /**
     * 简单出入场,停车1小时，无月票，钱包手动支付
     * 测试前数据:13807721234,钱包余额有足够余额
     * @throws Exception 
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testSimpleInOut() throws Exception
    {
        DateTime inTime = new DateTime();
        DateTime outTime = inTime.plusMinutes(90);
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis());
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场位置
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableCnt();
        
        //车辆入场
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventIn))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        String retContent = result.getResponse().getContentAsString();
        //检查开闸入场
        BoyueRespWrap boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //登录
        String token = login();
        
        //反向寻车
        result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/cars/getParkLocation/%d", car.getCarId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        RestResult<ParkLocationVo> parkLocationResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<ParkLocationVo>>(){});
        //检查停车场
        assertEquals("parkNameFixed", parkLocationResult.getData().getName());
        
        //检查停车场空位-1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(availableCnt-1, park.getAvailableCnt());
        availableCnt = park.getAvailableCnt();
        
        //车辆出场
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis());
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        //检查不开闸,等待支付
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("notok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查停车场空位数量不变
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(availableCnt, park.getAvailableCnt());
        
        //检查订单费用
        result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/orders/parking/needToPayByCar/%d", car.getCarId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        RestResult<OrderVo> orderVoResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<OrderVo>>(){});
        assertEquals(new BigDecimal(5).setScale(2), orderVoResult.getData().getAmt());
        assertEquals(new BigDecimal(5).setScale(2), orderVoResult.getData().getRealUnpayedAmt());
        
        //检查comet轮询不抬杆
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/comet")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("serialno", boyueEventOut.getAlarmInfoPlate().getSerialno())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("notok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查停车场空位数量不变
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(availableCnt, park.getAvailableCnt());

        //反向寻车
        result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/cars/getParkLocation/%d", car.getCarId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        parkLocationResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<ParkLocationVo>>(){});
        //检查停车场
        assertEquals("parkNameFixed", parkLocationResult.getData().getName());
        
        //获取付款前钱包余额
        result = mockMvc.perform(MockMvcRequestBuilders.get("/users/my/walletBalance")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(boyueEventIn))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        RestResult<BigDecimal> balanceResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<BigDecimal>>(){});
        BigDecimal balance = balanceResult.getData();
        
        //不使用优惠券付费
        PayOrderParam payParam = new PayOrderParam();
        payParam.setOrderId(orderVoResult.getData().getOrderId());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/orders/walletPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(payParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        RestResult<Object> payResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<Object>>(){});
        assertEquals(Const.RET_OK, payResult.getRetCode());
        
        //校验订单金额
        Order order = orderService.findOneByOrderId(orderVoResult.getData().getOrderId());
        assertEquals(new BigDecimal(5).setScale(2), order.getAmt());
        assertEquals(new BigDecimal(5).setScale(2), order.getPayedAmt());
        assertEquals(new BigDecimal(5).setScale(2), order.getRealPayedAmt());
        assertEquals(BigDecimal.ZERO, order.getRealUnpayedAmt());
        
        //获取付款后钱包余额
        result = mockMvc.perform(MockMvcRequestBuilders.get("/users/my/walletBalance")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        balanceResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<BigDecimal>>(){});
        assertEquals(balance.subtract(order.getPayedAmt()), balanceResult.getData());
        
        //检查轮询抬杆
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/comet")
                .contentType(MediaType.APPLICATION_JSON)
                .param("serialno", boyueEventOut.getAlarmInfoPlate().getSerialno())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查停车场空位数量+1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(availableCnt+1, park.getAvailableCnt());
        
        //检查轮询不抬杆
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/comet")
                .contentType(MediaType.APPLICATION_JSON)
                .param("serialno", boyueEventOut.getAlarmInfoPlate().getSerialno())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("notok", boyueRespWrap.getBoyueResp().getInfo());
    }
    
}
