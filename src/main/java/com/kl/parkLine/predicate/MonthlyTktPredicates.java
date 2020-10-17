package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QMonthlyTkt;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.MonthlyTktVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class MonthlyTktPredicates
{
    public Predicate fuzzy(MonthlyTktVo MonthlyTktVo, User user) 
    {
        QMonthlyTkt qMonthlyTkt = QMonthlyTkt.monthlyTkt;
        BooleanBuilder where = new BooleanBuilder();
        
        //停车场名称
        if (!StringUtils.isEmpty(MonthlyTktVo.getParkName()))
        {
            where.and(qMonthlyTkt.park.name.containsIgnoreCase(MonthlyTktVo.getParkName()));
        }
        
        //车牌号
        if (!StringUtils.isEmpty(MonthlyTktVo.getCarNo()))
        {
            where.and(qMonthlyTkt.car.carNo.containsIgnoreCase(MonthlyTktVo.getCarNo()));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    private Predicate roleFilter(User user) 
    {
        //TODO:停车场人员只能看自己的数据, 普通用户只能看自己的数据, 管理员看所有数据
        QMonthlyTkt qMonthlyTkt = QMonthlyTkt.monthlyTkt;
        BooleanBuilder where = new BooleanBuilder();
        
        //普通用户只能看自己车辆的订单
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qMonthlyTkt.car.in(user.getCars()));
        }
        
        return where;
    }
}
