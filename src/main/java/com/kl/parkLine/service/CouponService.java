package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.CompareUtil;
import com.kl.parkLine.dao.ICouponDao;
import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.CouponLog;
import com.kl.parkLine.entity.QCoupon;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.CouponPredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.CouponVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
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
    private CompareUtil compareUtil;
    
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
        
        //新增优惠券
        Coupon coupon = new Coupon();
        coupon.setCouponDef(couponDef);
        coupon.setCode(String.format("YHJ%s", String.valueOf(now.getMillis())));
        coupon.setOwner(user);
        coupon.setStartDate(couponDef.getStartDate());
        coupon.setEndDate(couponDef.getEndDate());
        coupon.setStatus(CouponStatus.valid);
        this.save(coupon, coupon.getCode());
        couponDao.save(coupon);
        
        //增加优惠券定义的已经领取数量
        couponDef.setAppliedCnt(couponDef.getAppliedCnt()+1);
        couponDef.setChangeRemark(String.format("CouponCode: %s", coupon.getCode()));
        couponDefService.save(couponDef);
        
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
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qCoupon.couponId,
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
                        qCoupon.endDate
                )
                .from(qCoupon)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<CouponVo> couponVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> CouponVo.builder()
                        .couponId(tuple.get(qCoupon.couponId))
                        .code(tuple.get(qCoupon.code))
                        .couponDefId(tuple.get(qCoupon.couponDef.couponDefId))
                        .couponDefCode(tuple.get(qCoupon.couponDef.code))
                        .name(tuple.get(qCoupon.couponDef.name))
                        .amt(tuple.get(qCoupon.couponDef.amt))
                        .minAmt(tuple.get(qCoupon.couponDef.minAmt))
                        .owner(tuple.get(qCoupon.owner.name))
                        .startDate(tuple.get(qCoupon.startDate))
                        .endDate(tuple.get(qCoupon.endDate))
                        .status(tuple.get(qCoupon.status).getText())
                        .build()
                        )
                .collect(Collectors.toList());
        return new PageImpl<>(couponVos, pageable, queryResults.getTotal());
    }
}
