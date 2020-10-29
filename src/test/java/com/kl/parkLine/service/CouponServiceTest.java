package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.exception.BusinessException;

@SpringBootTest
public class CouponServiceTest
{
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private OrderService orderService;
     
    @Test
    @Transactional
    public void testFindBest4Order() throws ParseException
    {
        Order order = orderService.findOneByOrderId(1);
        Coupon coupon = couponService.findBest4Order(order);
        assertEquals(coupon.getCode(), "YHJ1603933619895");
    }
    
}
