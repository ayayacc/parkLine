package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QPark;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.vo.ParkVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class ParkPredicates
{
    public Predicate fuzzy(ParkVo parkVo, User user) 
    {
        QPark qPark = QPark.park;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(parkVo.getCode()))
        {
            where.and(qPark.code.containsIgnoreCase(parkVo.getCode()));
        }
        
        //名称
        if (!StringUtils.isEmpty(parkVo.getName()))
        {
            where.and(qPark.name.containsIgnoreCase(parkVo.getName()));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    //TODO:角色过滤，停车场管理员只能看到自己的停车场
    private Predicate roleFilter(User user) 
    {
        BooleanBuilder where = new BooleanBuilder();
        /*QPark qPark = QPark.park;
        
        if (user.hasRole(RoleCode.PARK_ADMIN))
        {
            where.and(qPark.car.in(user.getCars()));
        }*/
        
        return where;
    }
}
