package com.kl.parkLine.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kl.parkLine.service.CouponService;

@Component
public class ScheduleTask
{
    @Autowired
    private CouponService couponService;
    
    /**
     * 每天00:00:01秒执行,更新优惠券以及优惠券定义过期状态
     */
    @Scheduled(cron = "1 00 00 * * ?")
    public void updateCouponStatus()
    {
        //刷新优惠券状态
        couponService.updateExpiredStatus();
    }
}
