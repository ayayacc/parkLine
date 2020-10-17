package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QCouponDef;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.vo.MenuVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class MenuPredicates
{
    public Predicate fuzzy(MenuVo menuVo, User user) 
    {
        QCouponDef qCouponDef = QCouponDef.couponDef;
        BooleanBuilder where = new BooleanBuilder();
        
        //名称
        if (false == StringUtils.isEmpty(menuVo.getName()))
        {
            where.and(qCouponDef.name.containsIgnoreCase(menuVo.getName()));
        }
        
        return where;
    }
}
