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
    
    /**
     * 模糊匹配订单
     * @param orderVo
     * @param user
     * @return
     */
    private Predicate fuzzy(OrderVo orderVo, User user) 
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
        if (!StringUtils.isEmpty(orderVo.getCarCarNo()))
        {
            where.and(qOrder.car.carNo.containsIgnoreCase(orderVo.getCarCarNo()));
        }
        
        return where;
    }
    
    public Predicate fuzzyAsEndUser(OrderVo orderVo, User user) 
    {
        QOrder qOrder = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        where.and(fuzzy(orderVo, user));
        
        //区分用户权限
        where.and(qOrder.owner.eq(user))
                .or(qOrder.owner.isNull().and(qOrder.car.in(user.getCars())));
        
        return where;
    }
    
    public Predicate fuzzyAsManager(OrderVo orderVo, User user) 
    {
        QOrder qOrder = QOrder.order;
        BooleanBuilder where = new BooleanBuilder();
        where.and(fuzzy(orderVo, user));
        
        //区分用户权限
        where.and(qOrder.park.in(user.getParks()));
        
        return where;
    }
}
