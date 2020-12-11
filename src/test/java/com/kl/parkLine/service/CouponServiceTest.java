package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.Order;

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
        assertEquals(coupon.getCode(), "YHJ1606120006999");
    }
    
    @Test
    @Rollback(true)
    public void testUpdateExpiredStatus()
    {
        couponService.updateExpiredStatus();
    }
}
