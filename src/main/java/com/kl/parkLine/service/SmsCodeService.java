package com.kl.parkLine.service;

import java.util.Date;
import java.util.Set;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.SmsCmpt;
import com.kl.parkLine.dao.ISmsCodeDao;
import com.kl.parkLine.entity.SmsCode;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 *
 */
@Service("smsCodeService")
public class SmsCodeService
{
    @Autowired
    private ISmsCodeDao smsCodeDao;
    
    @Autowired
    private SmsCmpt smsCmpt;
    
    /**
     * 
     * @return
     */
    /**
     * 发送验证码
     * @param mobile 手机号码
     * @return
     */
    @Transactional
    public String sendSmsCode(String mobile) throws BusinessException
    {
        //找到最新发送的消息
        SmsCode lastSmsCode = findLastByMobile(mobile);
        if (null != lastSmsCode)
        {
            //校验是否频繁发送
            DateTime lastSendDate = new DateTime(lastSmsCode.getCreatedDate());
            DateTime resendLine = lastSendDate.plusMinutes(Const.VALID_CODE_MIN_INTERVAL); //能够重新发送的最早时间
            if (resendLine.isAfterNow()) //未到可以重发时间
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
        //TODO: 完善短消息内容：您正在登陆xxx，验证码:xxx,1分钟有效
        smsCmpt.sendSms(mobile, String.format("短信验证码:%s", code));
        
        return newCode.getCode();
    }
    
    @Transactional(readOnly = true)
    public SmsCode findLastByMobile(String mobile)
    {
        return smsCodeDao.findTop1ByMobileAndEnabledOrderByCreatedDateDesc(mobile, "Y");
    }
}