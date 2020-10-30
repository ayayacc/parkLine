package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.ICouponDefDao;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.CouponDefLog;
import com.kl.parkLine.entity.QCouponDef;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.CouponDefPredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.CouponDefVo;
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
public class CouponDefService
{
    @Autowired
    private ICouponDefDao couponDefDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private CouponDefPredicates couponDefPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 保存一个优惠券定义
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    public void save(CouponDef couponDef) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == couponDef.getCouponDefId()) //新增数据
        {
            couponDef.setLogs(new ArrayList<CouponDefLog>());
        }
        else//编辑已有数据
        {
            //编辑优惠券定义，//合并字段
            Optional<CouponDef> couponDefDst = couponDefDao.findById(couponDef.getCouponDefId());
            
            if (false == couponDefDst.isPresent())
            {
                throw new BusinessException(String.format("无效的优惠券定义 Id: %d", couponDef.getCouponDefId()));
            }
            
            //记录不同点
            diff = util.difference(couponDefDst.get(), couponDef);
            
            BeanUtils.copyProperties(couponDef, couponDefDst.get(), util.getNullPropertyNames(couponDef));
            
            couponDef = couponDefDst.get();
        }
        
        //保存数据
        CouponDefLog log = new CouponDefLog();
        log.setDiff(diff);
        log.setRemark(couponDef.getChangeRemark());
        if (!StringUtils.isEmpty(diff)  //至少有一项内容时才添加日志
            || !StringUtils.isEmpty(couponDef.getChangeRemark()))
        {
            log.setCouponDef(couponDef);
            couponDef.getLogs().add(log);
        }
        couponDefDao.save(couponDef);
    }
    
    /**
     * 模糊匹配优惠券定义
     * @param couponDef  
     * @param pageable
     * @param auth
     * @return
     */
    public Page<CouponDefVo> fuzzyFindPage(CouponDefVo couponDefVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = couponDefPredicates.fuzzy(couponDefVo, user);
        
        QCouponDef qCouponDef = QCouponDef.couponDef;
        QueryResults<CouponDefVo> queryResults = jpaQueryFactory
                .select(Projections.bean(CouponDefVo.class, qCouponDef.couponDefId,
                        qCouponDef.code,
                        qCouponDef.code,
                        qCouponDef.totalCnt,
                        qCouponDef.usedCnt,
                        qCouponDef.enabled,
                        qCouponDef.startDate,
                        qCouponDef.endDate))
                .from(qCouponDef)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
}
