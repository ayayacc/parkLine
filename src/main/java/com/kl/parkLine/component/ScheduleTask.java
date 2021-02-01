package com.kl.parkLine.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.kl.parkLine.enums.AccessTokenType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.service.AccessTokenService;
import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.service.OrderService;

@Component
public class ScheduleTask
{
    @Value("${spring.profiles.active}")
    private String active;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AccessTokenService accessTokenService;
    
    /**
     * 每天00:00:01秒执行,更新优惠券以及优惠券定义过期状态
     */
    @Scheduled(cron = "1 00 00 * * ?")
    public void updateCouponStatus()
    {
        //刷新优惠券状态
        couponService.updateExpiredStatus();
    }
    
    /**
     * 每天00:00:01秒执行,刷新过期的月票状态
     */
    @Scheduled(cron = "2 00 00 * * ?")
    public void updateMontlyTkt()
    {
        //刷新过期的月票状态
        try
        {
            orderService.updateExpiredMonthlyTkt();
        }
        catch (BusinessException e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 每9分50秒检查一次token,10分钟内过期更新
     */
    @Scheduled(fixedRate = 590000)
    public void updateAccessToken()
    {
        if (!active.equals("pro"))
        {
            return;
        }
        //检查accesstoken
        try
        {
            for (AccessTokenType accessTokenType : AccessTokenType.values())
            {
                accessTokenService.getLatestToken(accessTokenType);
            }
            accessTokenService.getLatestToken(AccessTokenType.gzh);
        }
        catch (BusinessException e)
        {
            e.printStackTrace();
        }
    }
}
