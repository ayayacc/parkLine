package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.CompareUtil;
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
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

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
    private UserService userService;
    
    @Autowired
    private CompareUtil compareUtil;
    
    @Autowired
    private CouponDefPredicates couponDefPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 保存一个优惠券定义
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    @Transactional
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
            diff = compareUtil.difference(couponDefDst.get(), couponDef);
            
            BeanUtils.copyProperties(couponDef, couponDefDst.get(), compareUtil.getNullPropertyNames(couponDef));
            
            couponDef = couponDefDst.get();
        }
        
        //保存数据
        CouponDefLog log = new CouponDefLog();
        log.setDiff(diff);
        log.setRemark(couponDef.getChangeRemark());
        log.setCouponDef(couponDef);
        couponDef.getLogs().add(log);
        couponDefDao.save(couponDef);
    }
    
    /**
     * 模糊匹配优惠券定义
     * @param couponDef  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<CouponDefVo> fuzzyFindPage(CouponDefVo couponDefVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = couponDefPredicates.fuzzy(couponDefVo, user);
        
        QCouponDef qCouponDef = QCouponDef.couponDef;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qCouponDef.couponDefId,
                        qCouponDef.code,
                        qCouponDef.name,
                        qCouponDef.totalCnt,
                        qCouponDef.usedCnt,
                        qCouponDef.enabled,
                        qCouponDef.startDate,
                        qCouponDef.endDate
                )
                .from(qCouponDef)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<CouponDefVo> couponDefVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> CouponDefVo.builder()
                        .couponDefId(tuple.get(qCouponDef.couponDefId))
                        .code(tuple.get(qCouponDef.code))
                        .name(tuple.get(qCouponDef.name))
                        .totalCnt(tuple.get(qCouponDef.totalCnt))
                        .usedCnt(tuple.get(qCouponDef.usedCnt))
                        .enabled(tuple.get(qCouponDef.enabled))
                        .startDate(tuple.get(qCouponDef.startDate))
                        .endDate(tuple.get(qCouponDef.endDate))
                        .build()
                        )
                .collect(Collectors.toList());
        return new PageImpl<>(couponDefVos, pageable, queryResults.getTotal());
    }
}
