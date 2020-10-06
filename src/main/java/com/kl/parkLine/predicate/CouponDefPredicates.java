package com.kl.parkLine.predicate;

import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.QCouponDef;
import com.kl.parkLine.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class CouponDefPredicates
{
    private CouponDefPredicates() {}
    
    public static Predicate fuzzySearch(CouponDef couponDef, User user) 
    {
        QCouponDef qCouponDef = QCouponDef.couponDef;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (false == StringUtils.isEmpty(couponDef.getCode()))
        {
            where.and(qCouponDef.code.containsIgnoreCase(couponDef.getCode()));
        }
        
        //名称
        if (null != couponDef.getName())
        {
            where.and(qCouponDef.name.eq(couponDef.getName()));
        }
        
        return where;
    }
}
