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
import com.kl.parkLine.enums.AccessTokenType;
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
    
    @Value("${wx.app.id}")
    private String appId;
    
    @Value("${wx.app.secret}")
    private String appSecret;
    
    @Autowired
    private IAccessTokenDao accessTokenDaoDao;
    
    @Autowired
    private IWxFeignClient wxFeignClient;
    
    /**
     * 获取最新的token,如果当前时间距离到期时间小于10分钟，则重新获取token
     * @param type需要获取的令牌类型
     * @throws BusinessException 
     */
    public String getLatestToken(AccessTokenType accessTokenType) throws BusinessException
    {
        DateTime now = new DateTime();
        AccessToken token = accessTokenDaoDao.findTopByTypeOrderByValidTimeDesc(accessTokenType);
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
            String id = openId;
            String secret = openSecret;
            if (accessTokenType.equals(AccessTokenType.xcx))
            {
                id = appId;
                secret = appSecret;
            }
            WxAccessTokenResult result = wxFeignClient.getAccessToken(id, secret);
            if (!StringUtils.isEmpty(result.getErrmsg()))
            {
                throw new BusinessException(result.getErrmsg());
            }
            token.setToken(result.getAccessToken());
            token.setType(accessTokenType);
            token.setValidTime(now.plusSeconds(result.getExpiresIn()).toDate());
            accessTokenDaoDao.save(token);
        }
        return token.getToken();
    }
}
