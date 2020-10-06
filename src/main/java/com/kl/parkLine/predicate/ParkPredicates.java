package com.kl.parkLine.predicate;

import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.QPark;
import com.kl.parkLine.entity.User;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

public class ParkPredicates
{
    private ParkPredicates() {}
    
    public static Predicate fuzzySearch(Park park, User user) 
    {
        QPark qPark = QPark.park;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (false == StringUtils.isEmpty(park.getCode()))
        {
            where.and(qPark.code.containsIgnoreCase(park.getCode()));
        }
        
        //名称
        if (null != park.getName())
        {
            where.and(qPark.name.eq(park.getName()));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    //TODO:角色过滤，停车场管理员只能看到自己的停车场
    public static Predicate roleFilter(User user) 
    {
        BooleanBuilder where = new BooleanBuilder();
        /*QPark qPark = QPark.park;
        
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qPark.car.in(user.getCars()));
        }*/
        
        return where;
    }
}
