package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QCouponDef;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.vo.CouponDefVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class CouponDefPredicates
{
    public Predicate fuzzy(CouponDefVo couponDefVo, User user) 
    {
        QCouponDef qCouponDef = QCouponDef.couponDef;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(couponDefVo.getCode()))
        {
            where.and(qCouponDef.code.containsIgnoreCase(couponDefVo.getCode()));
        }
        
        //名称
        if (!StringUtils.isEmpty(couponDefVo.getName()))
        {
            where.and(qCouponDef.name.containsIgnoreCase(couponDefVo.getName()));
        }
        
        return where;
    }
}
