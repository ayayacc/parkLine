package com.kl.parkLine.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.kl.parkLine.service.CouponService;

@Component
public class StartupRunner implements CommandLineRunner
{
    @Autowired
    private CouponService couponService;

    @Override
    public void run(String... args) throws Exception
    {
        couponService.updateExpiredStatus();
    }

}
