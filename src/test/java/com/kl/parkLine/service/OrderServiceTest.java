package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
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
            orderService.calAmt(order);
            assertEquals(amt, order.getAmt().intValue());
        }
    }
}
