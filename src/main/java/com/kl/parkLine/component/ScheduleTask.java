package com.kl.parkLine.component;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.entity.Order;
import com.kl.parkLine.enums.AccessTokenType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.service.AccessTokenService;
import com.kl.parkLine.service.CouponService;
import com.kl.parkLine.service.OrderService;

@Component
public class ScheduleTask
{
    private final static Logger logger = LoggerFactory.getLogger(ScheduleTask.class);
    
    @Value("${spring.profiles.active}")
    private String active;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private AccessTokenService accessTokenService;
    
    @Autowired
    private WxCmpt wxCmpt;
    
    @Autowired
    private Environment environment;
    
    private final String ScheduleTaskPort = "8443";
    
    /**
     * 每天00:00:01秒执行,更新优惠券以及优惠券定义过期状态
     */
    @Scheduled(cron = "1 00 00 * * ?")
    public void updateCouponStatus()
    {
        if (environment.getProperty("server.port").equalsIgnoreCase(ScheduleTaskPort))
        {
            //刷新优惠券状态
            couponService.updateExpiredStatus();
        }
        
    }
    
    /**
     * 每天00:00:02秒执行,刷新过期的月票状态
     */
    @Scheduled(cron = "2 00 00 * * ?")
    public void updateMontlyTkt()
    {
        if (environment.getProperty("server.port").equalsIgnoreCase(ScheduleTaskPort))
        {
            //刷新过期的月票状态
            try
            {
                orderService.updateExpiredMonthlyTkt();
            }
            catch (BusinessException e)
            {
                logger.error("updateMontlyTkt failed:", e);
            }
        }
    }
    
    /**
     * 每9分50秒检查一次token,10分钟内过期更新
     */
    @Scheduled(fixedRate = 590000)
    public void updateAccessToken()
    {
        if (environment.getProperty("server.port").equalsIgnoreCase(ScheduleTaskPort))
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
                //accessTokenService.getLatestToken(AccessTokenType.gzh);
            }
            catch (BusinessException e)
            {
                logger.error("updateAccessToken failed:", e);
            }
        }
    }
    
    /**
     * 每天10:30:00执行,发送即将过期的月票提醒
     */
    @Scheduled(cron = "00 30 10 * * ?")
    @Transactional
    public void sendExiringMsg()
    {
        if (environment.getProperty("server.port").equalsIgnoreCase(ScheduleTaskPort))
        {
            List<Order> orders = orderService.findExpiringMonthlyTkt();
            for (Order order : orders)
            {
                try
                {
                    wxCmpt.sendMonthlyTktExpireNote(order);
                    logger.info(String.format("sendExiringMsg success, order code: %s", order.getCode()));
                }
                catch (BusinessException e)
                {
                    logger.error(String.format("sendExiringMsg failed, order code: %s, %s", order.getCode(), e.getMessage()));
                }
            }
        }
    }
}
