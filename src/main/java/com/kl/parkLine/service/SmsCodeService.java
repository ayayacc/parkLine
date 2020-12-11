package com.kl.parkLine.service;

import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.AliYunCmpt;
import com.kl.parkLine.dao.ISmsCodeDao;
import com.kl.parkLine.entity.SmsCode;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.security.SmsAuthenticationException;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SmsCodeService
{
    @Autowired
    private ISmsCodeDao smsCodeDao;
    
    @Autowired
    private AliYunCmpt aliYunCmpt;
    
    @Value("${spring.profiles.active}")
    private String active;
    
    /**
     * 
     * @return
     */
    /**
     * 发送验证码
     * @param mobile 手机号码
     * @return
     */
    public SmsCode sendSmsCode(String mobile) throws BusinessException
    {
        //找到最新发送的消息
        SmsCode lastSmsCode = findLastByMobile(mobile);
        if (null != lastSmsCode)
        {
            //校验是否频繁发送
            DateTime lastSendDate = new DateTime(lastSmsCode.getCreatedDate());
            DateTime resendLine = lastSendDate.plusMinutes(Const.VALID_CODE_MIN_INTERVAL); //能够重新发送的最早时间
            if (resendLine.isAfterNow() && !active.equalsIgnoreCase("dev")) //未到可以重发时间, dev环境不检查重发时限
            {
                throw new BusinessException("请求验证码过于频繁");
            }
            
            //禁用已经存在的验证码
            Set<SmsCode> smsCodes = smsCodeDao.findByMobileAndEnabled(mobile, "Y");
            for (SmsCode smsCode : smsCodes)
            {
                smsCode.setEnabled("N");
            }
            smsCodeDao.saveAll(smsCodes);
        }
        
        //写入新的验证码
        SmsCode newCode = new SmsCode();
        newCode.setMobile(mobile);
        //随机生成6位数字
        Integer code = (int)((Math.random()*9+1)*100000);
        newCode.setCode(code.toString());
        newCode.setEnabled("Y");
        Date expierTime = new DateTime().plusMinutes(Const.VALID_CODE_MIN_INTERVAL).toDate();
        newCode.setExpierTime(expierTime);
        smsCodeDao.save(newCode);
        
        //发送消息
        aliYunCmpt.sendValidCode(mobile, code.toString());
        
        return newCode;
    }
    
    public SmsCode findLastByMobile(String mobile)
    {
        return smsCodeDao.findTop1ByMobileAndEnabledOrderByCreatedDateDesc(mobile, "Y");
    }
    
    public void checkValidCode(String mobile, String code) throws BusinessException
    {
        //校验验证码
        SmsCode smsCode = findLastByMobile(mobile);
        if (null == smsCode)
        {
            throw new SmsAuthenticationException("无效验证码");
        }
        
        //比对验证码有效期和值
        Date now = new Date();
        if (smsCode.getExpierTime().before(now))
        {
            throw new SmsAuthenticationException("验证码已过期，请重新获取");
        }
        
        if (!smsCode.getCode().equalsIgnoreCase(code))
        {
            throw new SmsAuthenticationException("验证码不正确");
        }
        
        //设置验证码状态
        smsCode.setEnabled("N");
        smsCodeDao.save(smsCode);
    }
}
