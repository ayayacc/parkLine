package com.kl.parkLine.service;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.ICouponDao;
import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.QCoupon;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.CouponPredicates;
import com.kl.parkLine.vo.CouponVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
    private CouponDefService couponDefService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CouponPredicates couponPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 申请一个新的优惠券
     * @param couponDef 相关的优惠券定义
     * @param auth 当前登录用户
     * @return
     */
    @Transactional
    public Coupon apply(CouponDef couponDef, String userName) throws BusinessException
    {
        User user = userService.findByName(userName);
        
        DateTime now = new DateTime();
        
        //检查优惠券定义是否已经开始
        if (!couponDef.getStartDate().before(now.toDate()))
        {
            throw new BusinessException("领取失败，活动还未开始");
        }
        
        now.minusDays(1); //日期加一天，使得优惠券过期当天有效
        //检查优惠券定义是否过期
        if (couponDef.getEndDate().before(now.toDate()))
        {
            throw new BusinessException("领取失败，活动已过期");
        }
        
        //检查是否还有剩余
        Integer stk = couponDef.getTotalCnt() - couponDef.getAppliedCnt();
        if (0 >= stk)
        {
            throw new BusinessException("领取失败，优惠券已领完");
        }
        
        //检查是否已经有同类优惠券
        if (couponDao.existsByCouponDefCouponDefId(couponDef.getCouponDefId()))
        {
            throw new BusinessException("请勿重复领取");
        }
        
        //新增优惠券
        Coupon coupon = Coupon.builder()
                .couponDef(couponDef)
                .code(String.format("YHJ%s", String.valueOf(now.getMillis())))
                .owner(user)
                .startDate(couponDef.getStartDate())
                .endDate(couponDef.getEndDate())
                .status(CouponStatus.valid)
                .build();
        couponDao.save(coupon);
        
        //增加优惠券定义的已经领取数量
        couponDef.setAppliedCnt(couponDef.getAppliedCnt()+1);
        couponDef.setChangeRemark(String.format("新发优惠券, CouponCode: %s, 已经发放: %d 张, 剩余: %d 张",
                coupon.getCode(), couponDef.getAppliedCnt(), stk-1));
        couponDefService.save(couponDef);
        
        return coupon;
    }
    
    /**
     * 模糊查询
     * @param coupon  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<CouponVo> fuzzyFindPage(CouponVo couponVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = couponPredicates.fuzzy(couponVo, user);
        
        QCoupon qCoupon = QCoupon.coupon;
        QueryResults<CouponVo> queryResults = jpaQueryFactory
                .select(Projections.bean(CouponVo.class, qCoupon.couponId,
                        qCoupon.code,
                        qCoupon.couponDef.couponDefId,
                        qCoupon.couponDef.code,
                        qCoupon.couponDef.name,
                        qCoupon.couponDef.amt,
                        qCoupon.couponDef.minAmt,
                        qCoupon.status,
                        qCoupon.owner.name,
                        qCoupon.status,
                        qCoupon.startDate,
                        qCoupon.endDate))
                .from(qCoupon)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
    
    /**
     * 根据id查找唯一优惠券
     * @param couponId
     * @return
     */
    @Transactional
    public Coupon findOneById(Integer couponId)
    {
        return couponDao.findOneByCouponId(couponId);
    }
}
