package com.kl.parkLine.predicate;

import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QCouponDef;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.vo.CouponDefVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class CouponDefPredicates
{
    private CouponDefPredicates() {}
    
    public static Predicate fuzzySearch(CouponDefVo couponDefVo, User user) 
    {
        QCouponDef qCouponDef = QCouponDef.couponDef;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (false == StringUtils.isEmpty(couponDefVo.getCode()))
        {
            where.and(qCouponDef.code.containsIgnoreCase(couponDefVo.getCode()));
        }
        
        //名称
        if (false == StringUtils.isEmpty(couponDefVo.getName()))
        {
            where.and(qCouponDef.name.eq(couponDefVo.getName()));
        }
        
        return where;
    }
}
