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
        
        return where;
    }
}
