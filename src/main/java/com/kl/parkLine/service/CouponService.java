package com.kl.parkLine.service;

import java.util.Date;

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
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.QCoupon;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.enums.OrderStatus;
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
@Transactional(rollbackFor = Exception.class)
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
                .name(couponDef.getName())
                .minAmt(couponDef.getMinAmt())
                .amt(couponDef.getAmt())
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
    public Page<CouponVo> fuzzyFindPage(CouponVo couponVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = couponPredicates.fuzzy(couponVo, user);
        
        QCoupon qCoupon = QCoupon.coupon;
        QueryResults<CouponVo> queryResults = jpaQueryFactory
                .select(Projections.constructor(CouponVo.class, qCoupon.couponId,
                        qCoupon.couponDef.couponDefId,
                        qCoupon.couponDef.code.as("couponDefCode"),
                        qCoupon.couponDef.name.as("couponDefName"),
                        qCoupon.code,
                        qCoupon.name,
                        qCoupon.owner.name.as("ownerName"),
                        qCoupon.amt,
                        qCoupon.minAmt,
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
    public Coupon findOneById(Integer couponId)
    {
        return couponDao.findOneByCouponId(couponId);
    }
    
    /**
     * 根据订单找到最合适的优惠券,金额大到小,到期日小到大
     * @param order
     * @return
     */
    public Coupon findBest4Order(Order order)
    {
        return couponDao.findTopByOwnerAndStatusAndMinAmtLessThanEqualOrderByAmtDescEndDate(order.getOwner(), CouponStatus.valid, order.getAmt());
    }
    
    /**
     * 根据订单找到最合适的优惠券,金额大到小,到期日小到大
     * @param order
     * @return
     * @throws BusinessException 
     */
    public void useCoupon(Coupon coupon, String orderCode) throws BusinessException
    {
        //检查优惠券状态
        if (!coupon.getStatus().equals(CouponStatus.valid))
        {
            throw new BusinessException(String.format("优惠券: %s 处于: %状态,  不可用", 
                    coupon.getCode(), coupon.getStatus().getText()));
        }
        
        //设置优惠券状态
        coupon.setStatus(CouponStatus.used);
        
        //优惠券使用时间
        coupon.setUsedDate(new Date());
         
        //更新优惠券定义
        CouponDef couponDef = coupon.getCouponDef();
        Integer usedCnt = couponDef.getUsedCnt() + 1;
        couponDef.setChangeRemark(String.format("优惠券: %s 被订单: %s 使用, 使用数量: %d --> %d",
                coupon.getCode(), orderCode, couponDef.getUsedCnt(), usedCnt));
        couponDef.setUsedCnt(usedCnt);
        
        couponDefService.save(couponDef);
        
        couponDao.save(coupon);
        
        return;
    }

    /**
     * 根据订单找到可用的优惠券
     * @param order
     * @return
     */
    public Page<CouponVo> available4Order(Order order, Pageable pageable) throws BusinessException
    {
        if (!order.getStatus().equals(OrderStatus.needToPay))
        {
            throw new BusinessException(String.format("不能查询 %s 状态订单的可用优惠券", order.getStatus().getText()));
        }
        
        return couponDao.findByOwnerAndStatusAndMinAmtLessThanEqualOrderByAmtDescEndDate(order.getOwner(), CouponStatus.valid, order.getAmt(), pageable);
    }
}
