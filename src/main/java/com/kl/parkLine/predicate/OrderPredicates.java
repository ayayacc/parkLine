package com.kl.parkLine.predicate;

import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.util.RoleCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class OrderPredicates
{
    private OrderPredicates() {}
    
    public static Predicate fuzzySearch(Order order, User user) 
    {
        QOrder qOrder = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (false == StringUtils.isEmpty(order.getCode()))
        {
            where.and(qOrder.code.containsIgnoreCase(order.getCode()));
        }
        
        //状态
        if (null != order.getStatus())
        {
            where.and(qOrder.status.eq(order.getStatus()));
        }
        
        //停车场
        if (null != order.getPark())
        {
            where.and(qOrder.park.parkId.eq(order.getPark().getParkId()));
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
