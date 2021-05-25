package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.MonthlyTktParam;

@SpringBootTest
public class OrderServiceTest
{
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private IOrderDao orderDao;
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private ParkService parkService;
     
    @Test
    public void testSave() throws BusinessException
    {
        Order order = orderService.findOneByOrderId(5);
        order.setAmt(new BigDecimal(5));
        orderService.save(order);
    }
    
    @Test
    public void calMonth() throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DateTime dateTimeStart = new DateTime(sdf.parse("2020-10-01"));
        DateTime dateTimeEnd = new DateTime(sdf.parse("2021-12-31"));
        int month = (dateTimeEnd.getYear()-dateTimeStart.getYear())*12 + dateTimeEnd.getMonthOfYear()-dateTimeStart.getMonthOfYear() + 1;
        assertEquals(15, month);
    }
    
    @Test
    public void testInqueryMonthlyTkt() throws Exception
    {
        MonthlyTktParam monthlyTktParam = new MonthlyTktParam();
        Park park = parkService.findOneByCode("FixedPark01");
        monthlyTktParam.setParkId(park.getParkId());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        //30天整月
        //燃油车
        Car fuelCar = carService.getCar("桂B11111", PlateColor.blue);
        monthlyTktParam.setCarId(fuelCar.getCarId());
        Date startDate = sdf.parse("2021-11-01");
        monthlyTktParam.setStartDate(startDate);
        Date endDate = sdf.parse("2021-11-30");
        monthlyTktParam.setEndDate(endDate);
        BigDecimal amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(300).setScale(2), amt);
        //新能源车
        Car newEnergyCar = carService.getCar("桂B22222新", PlateColor.green);
        monthlyTktParam.setCarId(newEnergyCar.getCarId());
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(280.00).setScale(2), amt);
        
        //月内15天
        //燃油车
        monthlyTktParam.setCarId(fuelCar.getCarId());
        startDate = sdf.parse("2021-10-01");
        monthlyTktParam.setStartDate(startDate);
        endDate = sdf.parse("2021-10-15");
        monthlyTktParam.setEndDate(endDate);
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(145.16f).setScale(2, RoundingMode.HALF_UP), amt);
        //新能源车
        monthlyTktParam.setCarId(newEnergyCar.getCarId());
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(135.48).setScale(2, RoundingMode.HALF_UP), amt);
        
        //跨2个整月
        //燃油车
        monthlyTktParam.setCarId(fuelCar.getCarId());
        startDate = sdf.parse("2021-10-01");
        monthlyTktParam.setStartDate(startDate);
        endDate = sdf.parse("2021-11-30");
        monthlyTktParam.setEndDate(endDate);
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(600).setScale(2, RoundingMode.HALF_UP), amt);
        //新能源车
        monthlyTktParam.setCarId(newEnergyCar.getCarId());
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(560).setScale(2, RoundingMode.HALF_UP), amt);
        
        //跨月32天
        //燃油车
        monthlyTktParam.setCarId(fuelCar.getCarId());
        startDate = sdf.parse("2021-10-15");
        monthlyTktParam.setStartDate(startDate);
        endDate = sdf.parse("2021-11-15");
        monthlyTktParam.setEndDate(endDate);
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(314.52).setScale(2, RoundingMode.HALF_UP), amt);
        //新能源车
        monthlyTktParam.setCarId(newEnergyCar.getCarId());
        amt = orderService.inqueryMonthlyTkt(monthlyTktParam);
        assertEquals(new BigDecimal(293.55).setScale(2, RoundingMode.HALF_UP), amt);
    }
    
    @Test
    public void initOrder() throws BusinessException, ParseException
    {
        //固定费率燃油车订单
        initFixFuel();
        
        //固定费率新能源车订单
        initFixNewEnergy();
        
        //阶梯燃油车
        initStepFuel();
        
        //阶梯新能源车
        initStepNewEnergy();
    }
    
    @Test
    public void initMonthlyTkt() throws BusinessException, ParseException
    {
        Park fixedPark = parkService.findOneByCode("FixedPark01");
        Car car = carService.getCar("桂B11111", PlateColor.blue);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        //月票2021-01-01---2021-01-10
        String code = "固定费率_2021-01-01---2021-01-10";
        Order order = orderDao.findOneByCode(code); 
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.monthlyTicket);
        order.setStatus(OrderStatus.payed);
        order.setPark(fixedPark);
        order.setCar(car);
        Date startDate = simpleDateFormat.parse("2021-01-01");
        Date endDate = simpleDateFormat.parse("2021-01-10");
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        orderService.save(order);
        
        //月票2021-01-15---2021-01-30
        code = "固定费率_2021-01-15---2021-01-30";
        order = orderDao.findOneByCode(code); 
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.monthlyTicket);
        order.setStatus(OrderStatus.payed);
        order.setPark(fixedPark);
        order.setCar(car);
        startDate = simpleDateFormat.parse("2021-01-15");
        endDate = simpleDateFormat.parse("2021-01-30");
        order.setStartDate(startDate);
        order.setEndDate(endDate);
        orderService.save(order);
    }
    
    @Test
    public void testGetMinutes() throws BusinessException, ParseException
    {
        
        Park fixedPark = parkService.findOneByCode("FixedPark01");
        Car car = carService.getCar("桂B11111", PlateColor.blue);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Order order = new Order();
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(car);
        
        //固定费率_60m, 出入场都在月票开始之前
        Date inTime = simpleDateFormat.parse("2020-12-31 22:59:08");
        Date outTime = simpleDateFormat.parse("2020-12-31 23:59:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        Integer minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60, minutes);
        
        //固定费率_60m, 入场在月票开始前，出场在月票开始时刻
        inTime = simpleDateFormat.parse("2020-12-31 22:59:08");
        outTime = simpleDateFormat.parse("2021-01-01 00:00:00");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60, minutes);
        
        //固定费率_60m, 入场在月票开始前，出场在月票结束前
        inTime = simpleDateFormat.parse("2020-12-31 22:59:08");
        outTime = simpleDateFormat.parse("2021-01-05 23:59:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60, minutes);
        
        //固定费率_60m, 入场在月票开始前，出场在月票结束后
        inTime = simpleDateFormat.parse("2020-12-31 23:29:08");
        outTime = simpleDateFormat.parse("2021-01-11 00:30:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60, minutes);
        
        //固定费率_0m, 入场在月票开始后，出场在月票结束前
        inTime = simpleDateFormat.parse("2021-01-02 23:30:08");
        outTime = simpleDateFormat.parse("2021-01-8 00:30:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(0, minutes);
        
        //固定费率_60m, 入场在月票开始后，出场在月票结束后
        inTime = simpleDateFormat.parse("2021-01-02 23:30:08");
        outTime = simpleDateFormat.parse("2021-01-11 01:00:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60, minutes);
        
        //固定费率_60m, 出入场在月票结束后
        inTime = simpleDateFormat.parse("2021-01-11 08:30:08");
        outTime = simpleDateFormat.parse("2021-01-11 09:30:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60, minutes);
        
        //固定费率_60+4*24*60m, 入场在月票1开始前，出场在月票2中
        inTime = simpleDateFormat.parse("2020-12-31 22:59:08");
        outTime = simpleDateFormat.parse("2021-01-15 09:30:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60+4*24*60, minutes);
        
        //固定费率_4*24*60+60*2, 入场在月票1开始前，出场在月票2结束后
        inTime = simpleDateFormat.parse("2020-12-31 22:59:08");
        outTime = simpleDateFormat.parse("2021-01-31 01:00:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(60+4*24*60+60, minutes);
        
        //固定费率_4*24*60m, 入场在月票1中，出场在月票2中
        inTime = simpleDateFormat.parse("2021-01-02 08:30:08");
        outTime = simpleDateFormat.parse("2021-01-30 09:30:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(4*24*60, minutes);
        
        //固定费率_4*24*60m, 入场在月票1中，出场在月票2结束后
        inTime = simpleDateFormat.parse("2021-01-02 08:30:08");
        outTime = simpleDateFormat.parse("2021-01-31 01:00:08");
        order.setInTime(inTime);
        order.setOutTime(outTime);
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(4*24*60+60, minutes);
        
        order = orderService.findOneByOrderId(72668);
        minutes = orderService.getParkingMinutes(order, new DateTime(order.getOutTime()));
        assertEquals(0, minutes);
        
        order = orderService.findOneByOrderId(79795);
        outTime = simpleDateFormat.parse("2021-05-18 08:29:53");
        minutes = orderService.getParkingMinutes(order, new DateTime(outTime));
        assertEquals(0, minutes);
    }
    
    @Test
    public void testMinutesBetween() throws ParseException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTime startPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:00:01"));
        DateTime endPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:00:59"));
        assertEquals(0, Minutes.minutesBetween(startPoint, endPoint).getMinutes());
        
        startPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:00:01"));
        endPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:01:00"));
        assertEquals(0, Minutes.minutesBetween(startPoint, endPoint).getMinutes());
        
        startPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:00:01"));
        endPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:01:01"));
        assertEquals(1, Minutes.minutesBetween(startPoint, endPoint).getMinutes());
        
        startPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:00:10"));
        endPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:01:09"));
        assertEquals(0, Minutes.minutesBetween(startPoint, endPoint).getMinutes());
        
        startPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:00:10"));
        endPoint = new DateTime(simpleDateFormat.parse("2021-01-10 10:01:18"));
        assertEquals(1, Minutes.minutesBetween(startPoint, endPoint).getMinutes());
    }
    
    @Test
    public void testDaysBetween() throws ParseException
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateTime startPoint = new DateTime(simpleDateFormat2.parse("2021-04-29 10:12:48"));
        DateTime endPoint = new DateTime(simpleDateFormat.parse("2021-04-30"));
        int days = Days.daysBetween(new LocalDate(startPoint), new LocalDate(endPoint)).getDays();
        assertEquals(0, days);
    }
    
    //固定费率燃油车订单
    private void initFixFuel() throws ParseException, BusinessException 
    {
        Park fixedPark = parkService.findOneByCode("FixedPark01");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2020-11-01 08:12:40");
        DateTime inTime = new DateTime(date);
        //燃油车
        Car fuelCar = carService.getCar("桂B11111", PlateColor.blue);
        String code = "固定费率_燃油车_40m(1h)_免费";
        Order order = orderDao.findOneByCode(code); 
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车40分钟
        Date outTime = inTime.plusMinutes(40).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_70m(2h)_5元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车70分钟
        outTime = inTime.plusMinutes(70).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_130m(3h)_10元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车130分钟
        outTime = inTime.plusMinutes(130).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_380m(7h)_30元";//压封顶线
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车380分钟
        outTime = inTime.plusMinutes(380).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_490m(9h)_30元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车490分钟
        outTime = inTime.plusMinutes(490).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_1420m(24h)_30元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车1420分钟
        outTime = inTime.plusMinutes(1420).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_1460m(25h)_30元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车1460分钟
        outTime = inTime.plusMinutes(1460).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_燃油车_1520m(26h)_35元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车1520分钟
        outTime = inTime.plusMinutes(1520).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
    }
    
    //阶梯费率燃油车订单
    private void initStepFuel() throws ParseException, BusinessException 
    {
        Park stepPark = parkService.findOneByCode("StepPark01");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2020-11-01 08:12:40");
        DateTime inTime = new DateTime(date);
        //燃油车
        Car fuelCar = carService.getCar("桂B11111", PlateColor.blue);
        String code = "阶梯费率_燃油车_40m(1h)_免费";
        Order order = orderDao.findOneByCode(code); 
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车40分钟
        Date outTime = inTime.plusMinutes(40).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "阶梯费率_燃油车_70m(2h)_10元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车70分钟
        outTime = inTime.plusMinutes(70).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "阶梯费率_燃油车_130m(3h)_20元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车130分钟
        outTime = inTime.plusMinutes(130).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "阶梯费率_燃油车_380m(7h)_30元";//压封顶线
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(fuelCar);
        order.setInTime(inTime.toDate());
        //停车380分钟
        outTime = inTime.plusMinutes(380).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
    }
    
    //固定费率新能源车订单
    private void initFixNewEnergy() throws ParseException, BusinessException 
    {
        Park fixedPark = parkService.findOneByCode("FixedPark01");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2020-11-01 08:12:40");
        DateTime inTime = new DateTime(date);
        //新能源车
        Car newEnergyCar = carService.getCar("桂B22222新", PlateColor.green);
        String code = "固定费率_新能源车_40m(1h)_免费";
        Order order = orderDao.findOneByCode(code); 
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车40分钟
        Date outTime = inTime.plusMinutes(40).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_70m(2h)_3元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车70分钟
        outTime = inTime.plusMinutes(70).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_130m(3h)_6元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车130分钟
        outTime = inTime.plusMinutes(130).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_380m(7h)_18元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车380分钟
        outTime = inTime.plusMinutes(380).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_490m(9h)_20元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车490分钟
        outTime = inTime.plusMinutes(490).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_1420m(24h)_20元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车1420分钟
        outTime = inTime.plusMinutes(1420).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_1460m(25h)_20元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车1460分钟
        outTime = inTime.plusMinutes(1460).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "固定费率_新能源车_1520m(26h)_23元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(fixedPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车1520分钟
        outTime = inTime.plusMinutes(1520).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
    }
    
    private void initStepNewEnergy() throws ParseException, BusinessException
    {
        Park stepPark = parkService.findOneByCode("StepPark01");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = sdf.parse("2020-11-01 08:12:40");
        DateTime inTime = new DateTime(date);
        //新能源车
        Car newEnergyCar = carService.getCar("桂B22222新", PlateColor.green);
        String code = "阶梯费率_燃油车_40m(1h)_免费";
        Order order = orderDao.findOneByCode(code); 
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车40分钟
        Date outTime = inTime.plusMinutes(40).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "阶梯费率_燃油车_70m(2h)_8元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车70分钟
        outTime = inTime.plusMinutes(70).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "阶梯费率_燃油车_130m(3h)_16元";
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车130分钟
        outTime = inTime.plusMinutes(130).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
        
        code = "阶梯费率_燃油车_380m(7h)_24元";//压封顶线
        order = orderDao.findOneByCode(code);
        if (null == order)
        {
            order = new Order();
            order.setCode(code);
        }
        order.setType(OrderType.parking);
        order.setPark(stepPark);
        order.setCar(newEnergyCar);
        order.setInTime(inTime.toDate());
        //停车380分钟
        outTime = inTime.plusMinutes(380).toDate();
        order.setOutTime(outTime);
        orderService.save(order);
    }
    
    @Test
    @Transactional
    public void testCalAmt() throws ParseException, BusinessException
    {
        String casesStr[][] = {
            {"固定费率_燃油车_40m(1h)_免费", "0"},
            {"固定费率_燃油车_70m(2h)_5元", "5"},
            {"固定费率_燃油车_130m(3h)_10元", "10"},
            {"固定费率_燃油车_380m(7h)_30元", "30"},
            {"固定费率_燃油车_490m(9h)_30元", "30"},
            {"固定费率_燃油车_1420m(24h)_30元", "30"},
            {"固定费率_燃油车_1460m(25h)_30元", "30"},
            {"固定费率_燃油车_1520m(26h)_35元", "35"},
            {"固定费率_新能源车_40m(1h)_免费", "0"},
            {"固定费率_新能源车_70m(2h)_3元", "3"},
            {"固定费率_新能源车_130m(3h)_6元", "6"},
            {"固定费率_新能源车_380m(7h)_18元", "18"},
            {"固定费率_新能源车_490m(9h)_20元", "20"},
            {"固定费率_新能源车_1420m(24h)_20元", "20"},
            {"固定费率_新能源车_1460m(25h)_20元", "20"},
            {"固定费率_新能源车_1520m(26h)_23元", "23"},
            {"阶梯费率_燃油车_40m(1h)_免费", "0"},
            {"阶梯费率_燃油车_70m(2h)_10元", "10"},
            {"阶梯费率_燃油车_130m(3h)_20元", "20"},
            {"阶梯费率_燃油车_380m(7h)_30元", "30"},
            {"阶梯费率_燃油车_70m(2h)_8元", "8"},
            {"阶梯费率_燃油车_130m(3h)_16元", "16"},
            {"阶梯费率_燃油车_380m(7h)_24元", "24"}
        };
        
        for (String[] caseStr : casesStr)
        {
            String code = caseStr[0];
            System.out.println(code);
            Integer amt = Integer.valueOf(caseStr[1]);
            Order order = orderDao.findOneByCode(code);
            orderService.setAmtAndOutTimeLimit(order);
            assertEquals(amt, order.getAmt().intValue());
        }
    }
    
    @Test
    public void testCalDiscount()
    {
        BigDecimal amt = new BigDecimal(8);
        BigDecimal discountRate = new BigDecimal(8);
        BigDecimal max = new BigDecimal(1);
        BigDecimal discount = amt.multiply(new BigDecimal(10).subtract(discountRate).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP));
        BigDecimal realDiscount = max.min(discount);
        BigDecimal realAmt = amt.subtract(realDiscount);
        assertEquals(new BigDecimal(7), realAmt);
    }
    
    @Test
    public void testJoda()
    {
        DateTime now = new DateTime();
        DateTime future = now.plusDays(1).plusHours(1).plusMinutes(10).plusSeconds(50);
        Period period = new Period(now, future, PeriodType.time());
        int hours = 25;
        int minutes = 10;
        assertEquals(hours, period.getHours());
        assertEquals(minutes, period.getMinutes());
    }
    
    @Test
    public void testMd5() throws UnsupportedEncodingException
    {
        String parmas = "orderId=2&paymentTime=1610615042481&payee=1_admin测试&remark=2121&publicKey=parkLineAdmin&privateKey=DF09D78C73ECD21FCD2BDFD2877687BD";
        String md5 = DigestUtils.md5DigestAsHex(parmas.getBytes("UTF-8"));
        assertEquals("df54b6c7e0a3a6917eaff984da631bd9", md5);
    }
    
    @Test
    public void testFindExpiringMonthlyTkt() throws UnsupportedEncodingException
    {
        List<Order> orders = orderService.findExpiringMonthlyTkt();
        assertEquals(2, orders.size());
    }
    
    @Test
    @Rollback(true)
    @Transactional
    public void testCalAmt2() throws ParseException, BusinessException
    {
        Order order = orderService.findOneByOrderId(150);
        orderService.setAmtAndOutTimeLimit(order);
    }
    
    @Test
    public void testCompare() throws BusinessException
    {
        Order order = orderService.findOneByOrderId(70908);
        assertTrue(0 > order.getPayedAmt().compareTo(order.getAmt()));
    }
}
