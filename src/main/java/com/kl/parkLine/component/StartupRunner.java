package com.kl.parkLine.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.service.OrderService;

@Component
public class StartupRunner implements CommandLineRunner
{
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private OrderService orderService;

    @Override
    public void run(String... args) throws Exception
    {
        couponService.updateExpiredStatus();
        orderService.updateExpiredMonthlyTkt();
    }

}
