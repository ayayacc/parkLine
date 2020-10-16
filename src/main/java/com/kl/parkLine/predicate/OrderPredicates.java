package com.kl.parkLine.predicate;

import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.OrderVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class OrderPredicates
{
    private OrderPredicates() {}
    
    public static Predicate fuzzySearch(OrderVo orderVo, User user) 
    {
        QOrder qOrder = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(orderVo.getCode()))
        {
            where.and(qOrder.code.containsIgnoreCase(orderVo.getCode()));
        }
        
        //状态
        if (!StringUtils.isEmpty(orderVo.getStatus()))
        {
            where.and(qOrder.status.eq(OrderStatus.valueOf(orderVo.getStatus())));
        }
        
        //停车场
        if (!StringUtils.isEmpty(orderVo.getCarNo()))
        {
            where.and(qOrder.car.carNo.containsIgnoreCase(orderVo.getCarNo()));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    //角色过滤，普通用户只能看自己车辆的订单
    public static Predicate roleFilter(User user) 
    {
        QOrder qOrder = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        
        //普通用户只能看自己车辆的订单
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qOrder.car.in(user.getCars()));
        }
        
        return where;
    }
}
