package com.kl.parkLine.predicate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.OrderVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class OrderPredicates
{
    //停车场角色
    private final List<String> parkRoles = new ArrayList<String>();
    
    public OrderPredicates()
    {
        parkRoles.add(RoleCode.PARK_ADMIN);
        parkRoles.add(RoleCode.PARK_FINANCIAL);
        parkRoles.add(RoleCode.PARK_GUARD);
    }
    
    public Predicate fuzzy(OrderVo orderVo, User user) 
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
            where.and(qOrder.status.eq(orderVo.getStatus()));
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
    
    //角色过
    private Predicate roleFilter(User user) 
    {
        QOrder qOrder = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        
        //普通用户只能看自己车辆的订单(payer是自己 以及 车辆是自己的并且payer是空)
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qOrder.payer.eq(user)
                    .or(qOrder.payer.isNull().and(qOrder.car.in(user.getCars()))));
        }
        
        //停车场用户只能看自己的场地
        if (user.hasAnyRole(parkRoles))
        {
            where.or(qOrder.park.in(user.getParks()));
        }
        
        return where;
    }
}
