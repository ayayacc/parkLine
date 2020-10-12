package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.Optional;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.CompareUtil;
import com.kl.parkLine.dao.ICouponDao;
import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.CouponLog;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 *
 */
@Service
public class CouponService
{
    @Autowired
    private ICouponDao couponDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CouponDefService couponDefService;
    
    @Autowired
    private CompareUtil compareUtil;
    
    /**
     * 申请一个新的优惠券
     * @param couponDef 相关的优惠券定义
     * @param auth 当前登录用户
     * @return
     */
    @Transactional
    public Coupon apply(CouponDef couponDef, Authentication auth) throws BusinessException
    {
        DateTime now = new DateTime();
        
        //检查优惠券定义是否已经开始
        if (!couponDef.getStartDate().before(now.toDate()))
        {
            throw new BusinessException("领取失败，还未开始");
        }
        
        now.minusDays(1); //日期加一天，使得优惠券过期当天有效
        //检查优惠券定义是否过期
        if (couponDef.getEndDate().before(now.toDate()))
        {
            throw new BusinessException("领取失败，已过期");
        }
        
        //检查是否还有剩余
        Integer stk = couponDef.getTotalCnt() - couponDef.getUsedCnt();
        if (0 >= stk)
        {
            throw new BusinessException("领取失败，优惠券已领完");
        }
        
        //检查是否已经有同类优惠券
        if (couponDao.existsByCouponDefCouponDefId(couponDef.getCouponDefId()))
        {
            throw new BusinessException("请勿重复领取");
        }
        
        //获取当前用户
        User user = userService.findByName(auth.getName());
        
        //新增优惠券
        Coupon coupon = new Coupon();
        coupon.setCouponDef(couponDef);
        coupon.setCode(String.format("YHJ%s", String.valueOf(now.getMillis())));
        coupon.setMinAmt(couponDef.getMinAmt()); //满x元使用
        coupon.setAmt(couponDef.getAmt()); //优惠券金额
        coupon.setOwner(user);
        coupon.setStartDate(couponDef.getStartDate());
        coupon.setEndDate(couponDef.getEndDate());
        coupon.setStatus(CouponStatus.valid);
        this.save(coupon, coupon.getCode());
        couponDao.save(coupon);
        
        //增加优惠券定义的已经领取数量
        couponDef.setAppliedCnt(couponDef.getAppliedCnt()+1);
        couponDefService.save(couponDef, String.format("CouponCode: %s", coupon.getCode()));
        
        return coupon;
    }
    
    /**
     * 保存一个优惠券实例
     * @param 被保存的优惠券
     * @param remark 备注信息
     * @throws BusinessException 
     */
    @Transactional
    public void save(Coupon coupon, String remark) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == coupon.getCouponId()) //新增数据
        {
            coupon.setLogs(new ArrayList<CouponLog>());
        }
        else//编辑已有数据
        {
            //编辑优惠券定义，//合并字段
            Optional<Coupon> couponDst = couponDao.findById(coupon.getCouponId());
            
            if (false == couponDst.isPresent())
            {
                throw new BusinessException(String.format("无效的优惠券定义 Id: %d", coupon.getCouponId()));
            }
            
            //记录不同点
            diff = compareUtil.difference(couponDst.get(), coupon);
            
            BeanUtils.copyProperties(coupon, couponDst.get(), compareUtil.getNullPropertyNames(coupon));
            
            coupon = couponDst.get();
        }
        
        //保存数据
        CouponLog log = new CouponLog();
        log.setDiff(diff);
        log.setRemark(remark);
        log.setCoupon(coupon);
        coupon.getLogs().add(log);
        couponDao.save(coupon);
    }
}
