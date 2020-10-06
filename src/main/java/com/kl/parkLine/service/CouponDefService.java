package com.kl.parkLine.service;

import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.CompareUtil;
import com.kl.parkLine.dao.ICouponDefDao;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.CouponDefLog;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 *
 */
@Service
public class CouponDefService
{
    @Autowired
    private ICouponDefDao couponDefDao;
    
    @Autowired
    private CompareUtil compareUtil;
    
    /**
     * 保存一个优惠券定义
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    @Transactional
    public void save(CouponDef couponDef) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null != couponDef.getCouponDefId()) //编辑已有数据
        {
            //编辑停车场，//合并字段
            Optional<CouponDef> couponDefDst = couponDefDao.findById(couponDef.getCouponDefId());
            
            if (false == couponDefDst.isPresent())
            {
                throw new BusinessException(String.format("无效的优惠券定义 Id: %d", couponDef.getCouponDefId()));
            }
            
            //记录不同点
            diff = compareUtil.difference(couponDefDst.get(), couponDef);
            
            BeanUtils.copyProperties(couponDef, couponDefDst.get(), compareUtil.getNullPropertyNames(couponDef));
            
            couponDef = couponDefDst.get();
        }
        
        //保存数据
        CouponDefLog log = new CouponDefLog();
        log.setDiff(diff);
        log.setCouponDef(couponDef);
        couponDef.getLogs().add(log);
        couponDefDao.save(couponDef);
    }
}
