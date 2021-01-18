package com.kl.parkLine.service;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.dao.IAccessTokenDao;
import com.kl.parkLine.entity.AccessToken;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.feign.IWxFeignClient;
import com.kl.parkLine.json.WxAccessTokenResult;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class AccessTokenService
{
    @Value("${wx.open.id}")
    private String openId;
    
    @Value("${wx.open.secret}")
    private String openSecret;
    
    @Autowired
    private IAccessTokenDao accessTokenDaoDao;
    
    @Autowired
    private IWxFeignClient wxFeignClient;
    
    /**
     * 获取最新的token,如果当前时间距离到期时间小于10分钟，则重新获取token
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    public String getLatestToken() throws BusinessException
    {
        DateTime now = new DateTime();
        AccessToken token = accessTokenDaoDao.findTopByOrderByValidTimeDesc();
        Boolean needToUpdate = false;
        
        //从未获取token
        if (null == token)
        {
            needToUpdate = true;
            token = new AccessToken();
        }
        //token即将过期
        else if (10 >= Minutes.minutesBetween(now, new DateTime(token.getValidTime())).getMinutes())
        {
            needToUpdate = true;
        }
        
        //刷新token
        if (needToUpdate)
        {
            WxAccessTokenResult result = wxFeignClient.getAccessToken(openId, openSecret);
            if (!StringUtils.isEmpty(result.getErrmsg()))
            {
                throw new BusinessException(result.getErrmsg());
            }
            token.setToken(result.getAccessToken());
            token.setValidTime(now.plusSeconds(result.getExpiresIn()).toDate());
            accessTokenDaoDao.save(token);
        }
        return token.getToken();
    }
}
