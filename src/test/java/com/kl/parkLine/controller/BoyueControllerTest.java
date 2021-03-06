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
import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.enums.RetCode;
import com.kl.parkLine.json.CarParam;
import com.kl.parkLine.json.JwtToken;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.SmsCheckParam;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.service.DeviceService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.service.ParkService;
import com.kl.parkLine.service.UserService;
import com.kl.parkLine.vo.OrderVo;
import com.kl.parkLine.vo.ParkLocationVo;
import com.kl.parkLine.vo.SmsCodeVo;

@SpringBootTest
@AutoConfigureMockMvc
public class BoyueControllerTest
{
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private IOrderDao orderDao;
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private ParkService parkService;
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private UserService userService;
     
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
    public void testWalletPayNoCoupon() throws Exception
    {
        DateTime inTime = new DateTime();
        DateTime outTime = inTime.plusMinutes(90);
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场位置
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //关闭无感支付
        result = mockMvc.perform(MockMvcRequestBuilders.get("/users/my/setQuickPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .param("isQuickPay", "false")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        
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
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //车辆出场
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
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
        assertEquals(availableCnt, park.getAvailableTmpCnt());
        
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
        assertEquals(availableCnt, park.getAvailableTmpCnt());

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
        assertEquals(RetCode.ok, payResult.getRetCode());
        
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
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    /**
     * 钱包支付用优惠券
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testWalletPayWithCoupon() throws Exception
    {}
    
    
    /**
     * 非提前支付,停车1.5小时，无月票，无感支付成功
     * 测试前数据:13807721234,钱包余额有足够余额,用户开通无感支付
     * @throws Exception 
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testQuickPay() throws Exception
    {
        DateTime inTime = new DateTime();
        DateTime outTime = inTime.plusMinutes(90);
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场位置
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        
        //开通无感支付
        result = mockMvc.perform(MockMvcRequestBuilders.get("/users/my/setQuickPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .param("isQuickPay", "true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        
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
        
        Order order = orderDao.findTopByCarAndTypeAndIsOutIsFalseOrderByInTimeDesc(car, OrderType.parking);
        
        //车辆出场, 并且支付成功
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        //无感支付成功,开闸
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //校验订单金额
        order = orderService.findOneByOrderId(order.getOrderId());
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
        
        //检查停车场空位数量+1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    
    /**
     * 非提前支付,停车1.5小时，无月票，无感支付成功，找到最适合的优惠券
     * 测试前数据:13807721234,钱包余额有足够余额,用户开通无感支付
     * @throws Exception 
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testQuickPayWithCoupon() throws Exception
    {
        DateTime inTime = new DateTime();
        DateTime outTime = inTime.plusMinutes(90);
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场位置
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        
        //开通无感支付
        result = mockMvc.perform(MockMvcRequestBuilders.get("/users/my/setQuickPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .param("isQuickPay", "true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        
        //设置钱包余额满足优惠券后金额，不满足原金额
        User user = car.getUser();
        user.setBalance(new BigDecimal(4));
        userService.save(user);
        
        //领取优惠券, 预制3张优惠券couponDef4All-8, couponDef4All-9,couponDef4Park23-6
        for (int i=0; i<3; i++)
        {
            result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/coupons/apply/%d", i))
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", token)
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(MockMvcResultMatchers.status().isOk())
                    .andReturn();
        }
        
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
        
        //车辆出场, 并且支付成功
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        //无感支付成功,开闸
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //校验订单金额
        Order order = orderDao.findTopByPlateIdOrderByInTimeDescCreatedDateDesc(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateId());
        assertEquals(new BigDecimal(5).setScale(2), order.getAmt());
        assertEquals(new BigDecimal(5).setScale(2), order.getPayedAmt());
        Coupon coupon = order.getOrderPayments().get(0).getUsedCoupon();
        assertEquals("couponDef4All-8", coupon.getName());
        assertEquals(CouponStatus.used, coupon.getStatus());
        assertEquals(order.getAmt().subtract(coupon.getUsedAmt()), order.getRealPayedAmt());
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
        assertEquals(balance.subtract(order.getRealPayedAmt()), balanceResult.getData());
        
        //检查停车场空位数量+1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    /**
     * 无感支付钱包余额不够,停车1.5小时
     * 测试前数据:13807721234
     * @throws Exception 
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testQuickPayNoEnoughBalance() throws Exception
    {
        DateTime inTime = new DateTime();
        DateTime outTime = inTime.plusMinutes(90);
        //车辆入场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场位置
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        
        //开通无感支付
        result = mockMvc.perform(MockMvcRequestBuilders.get("/users/my/setQuickPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .param("isQuickPay", "true")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        
        //设置钱包余额不足以支付
        User user = car.getUser();
        user.setBalance(new BigDecimal(3));
        userService.save(user);
        
        //余额不足，无法出场
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        //余额不足，无感支付失败,不开闸
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("notok", boyueRespWrap.getBoyueResp().getInfo());
        
        //校验订单金额
        Order order = orderDao.findTopByCarAndTypeAndIsOutIsFalseOrderByInTimeDesc(car, OrderType.parking);
        assertEquals(new BigDecimal(5).setScale(2), order.getAmt());
        assertEquals(new BigDecimal(5).setScale(2), order.getRealUnpayedAmt());
    }
    
    
    /**
     * 提前支付未超时，不产生新费用
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testAdvancePay() throws Exception
    {
        DateTime inTime = new DateTime().minusMinutes(100); //100分钟前入场,提前支付100分钟停车费
        DateTime outTime = inTime.plusMinutes(125); //出场时, 停车125分钟跨到下个计费周期，但是由于提前交费未超时，只交一个周期费用
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场数量
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        assertEquals(RetCode.ok, payResult.getRetCode());
        
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
        
        //车辆出场, 抬杆
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查停车场空位数量+1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    /**
     * 提前支付超时,但是未到下个计费周期，正常出场
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testAdvancePayTimeOutNoMoreFee() throws Exception
    {
        DateTime inTime = new DateTime().minusMinutes(70); //70分钟前入场,提前支付70分钟停车费
        DateTime outTime = inTime.plusMinutes(110); //停车110后出场，超时，但是未到下个计费周期，正常出场
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场数量
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        assertEquals(RetCode.ok, payResult.getRetCode());
        
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
        
        //车辆出场, 抬杆
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查停车场空位数量+1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    /**
     * 提前支付超时产生费用
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testAdvancePayTimeOutMoreFee() throws Exception
    {
        DateTime inTime = new DateTime().minusMinutes(70); //70分钟前入场,提前支付70分钟停车费
        DateTime outTime = inTime.plusMinutes(130); //停车130后出场，超时，需要补交费
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场数量
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        assertEquals(RetCode.ok, payResult.getRetCode());
        
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
        
        //车辆出场, 不抬杆
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("notok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查订单费用,需要补缴5元
        result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/orders/parking/needToPayByCar/%d", car.getCarId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        orderVoResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<OrderVo>>(){});
        assertEquals(new BigDecimal(10).setScale(2), orderVoResult.getData().getAmt()); //整单金额10元
        assertEquals(new BigDecimal(5).setScale(2), orderVoResult.getData().getPayedAmt()); //整单金额10元
        assertEquals(new BigDecimal(5).setScale(2), orderVoResult.getData().getRealUnpayedAmt());
        assertEquals(new BigDecimal(5).setScale(2), order.getRealPayedAmt());

        //不使用优惠券付费
        payParam = new PayOrderParam();
        payParam.setOrderId(orderVoResult.getData().getOrderId());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/orders/walletPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(payParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        payResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<Object>>(){});
        assertEquals(RetCode.ok, payResult.getRetCode());
        
        //校验订单金额
        order = orderService.findOneByOrderId(orderVoResult.getData().getOrderId());
        assertEquals(new BigDecimal(10).setScale(2), order.getAmt());
        assertEquals(new BigDecimal(10).setScale(2), order.getPayedAmt());
        assertEquals(new BigDecimal(10).setScale(2), order.getRealPayedAmt());
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
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    /**
     * 提前支付超时,先用优惠券
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testAdvancePayTimeOutUseCouponBefore() throws Exception
    {
        DateTime inTime = new DateTime().minusMinutes(70); //70分钟前入场,提前支付70分钟停车费
        DateTime outTime = inTime.plusMinutes(130); //停车130后出场，超时，需要补交费
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场数量
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        
        //使用优惠券付费
        PayOrderParam payParam = new PayOrderParam();
        payParam.setCouponId(2);
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
        assertEquals(RetCode.ok, payResult.getRetCode());
        
        //校验订单金额
        Order order = orderService.findOneByOrderId(orderVoResult.getData().getOrderId());
        assertEquals(new BigDecimal(5).setScale(2), order.getAmt());
        assertEquals(new BigDecimal(5).setScale(2), order.getPayedAmt());
        Coupon coupon = order.getOrderPayments().get(0).getUsedCoupon();
        assertEquals("couponDef4All-9", coupon.getName());
        assertEquals(CouponStatus.used, coupon.getStatus());
        assertEquals(new BigDecimal(0.5).setScale(2), coupon.getUsedAmt());
        assertEquals(order.getRealPayedAmt(), order.getAmt().subtract(coupon.getUsedAmt()));
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
        assertEquals(balance.subtract(order.getRealPayedAmt()), balanceResult.getData());
        
        //车辆出场, 不抬杆
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("notok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查订单费用,需要补缴5元
        result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/orders/parking/needToPayByCar/%d", car.getCarId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        orderVoResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<OrderVo>>(){});
        assertEquals(new BigDecimal(10).setScale(2), orderVoResult.getData().getAmt()); //整单金额10元
        assertEquals(new BigDecimal(5).setScale(2), orderVoResult.getData().getPayedAmt()); //已经付款5元
        assertEquals(new BigDecimal(5).setScale(2), orderVoResult.getData().getRealUnpayedAmt());
        assertEquals(new BigDecimal(4.5).setScale(2), order.getRealPayedAmt());

        //补缴费用，不使用优惠券
        payParam = new PayOrderParam();
        payParam.setOrderId(orderVoResult.getData().getOrderId());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/orders/walletPay")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(payParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        payResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<Object>>(){});
        assertEquals(RetCode.ok, payResult.getRetCode());
        
        //校验订单金额
        order = orderService.findOneByOrderId(orderVoResult.getData().getOrderId());
        assertEquals(new BigDecimal(10).setScale(2), order.getAmt());
        assertEquals(new BigDecimal(10).setScale(2), order.getPayedAmt());
        assertEquals(new BigDecimal(9.5).setScale(2), order.getRealPayedAmt());
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
        assertEquals(balance.subtract(order.getRealPayedAmt()), balanceResult.getData());
        
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
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
    
    /**
     * 提前支付超时,后用优惠券
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testAdvancePayTimeOutUseCouponAfter() throws Exception
    {}

    /**
     * 未到计费周期，无需付款
     * @throws Exception
     */
    @Test
    @Transactional
    @Rollback(true)
    public void testInFreeTime() throws Exception
    {
        DateTime inTime = new DateTime();
        DateTime outTime = inTime.plusMinutes(40); //停车40分钟，免费
        Resource resource = new ClassPathResource("/testData/boyue/carIn.json");
        InputStream is = resource.getInputStream();
        BoyueEvent boyueEventIn = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(inTime.getMillis()/1000);
        is.close();
        Car car = carService.getCar(boyueEventIn.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo(), PlateColor.blue);
        
        //记录入场前停车场数量
        Device device = deviceService.findOneBySerialNo(boyueEventIn.getAlarmInfoPlate().getSerialno());
        Park park = device.getPark();
        Integer availableCnt = park.getAvailableTmpCnt();
        
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
        
        //检查停车场空位数量-1
        park = parkService.findOneById(park.getParkId());
        assertEquals(--availableCnt, park.getAvailableTmpCnt());
        
        //登录
        String token = login();
        
        //绑定车辆
        CarParam bindCarParam = new CarParam();
        bindCarParam.setCarNo(car.getCarNo());
        bindCarParam.setPlateColor(car.getPlateColor());
        result = mockMvc.perform(MockMvcRequestBuilders.post("/cars/bind")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .content(JSON.toJSONString(bindCarParam))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        
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
        
        //检查订单费用
        result = mockMvc.perform(MockMvcRequestBuilders.get(String.format("/orders/parking/needToPayByCar/%d", car.getCarId()))
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", token)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        RestResult<OrderVo> orderVoResult = JSONObject.parseObject(retContent, new TypeReference<RestResult<OrderVo>>(){});
        assertEquals(new BigDecimal(0).setScale(2), orderVoResult.getData().getAmt());
        assertEquals(new BigDecimal(0).setScale(2), orderVoResult.getData().getRealUnpayedAmt());
        
        //车辆出场, 抬杆
        resource = new ClassPathResource("/testData/boyue/carOut.json");
        is = resource.getInputStream();
        BoyueEvent boyueEventOut = JSONObject.parseObject(is, BoyueEvent.class);
        boyueEventOut.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().setSec(outTime.getMillis()/1000);
        is.close();
        result = mockMvc.perform(MockMvcRequestBuilders.post("/boyue/plateNotify")
                .contentType(MediaType.APPLICATION_JSON)
                .content(JSON.toJSONString(boyueEventOut))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        retContent = result.getResponse().getContentAsString();
        boyueRespWrap = JSONObject.parseObject(retContent, BoyueRespWrap.class);
        assertEquals("ok", boyueRespWrap.getBoyueResp().getInfo());
        
        //检查停车场空位数量+1
        park = parkService.findOneById(parkLocationResult.getData().getParkId());
        assertEquals(++availableCnt, park.getAvailableTmpCnt());
        
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
