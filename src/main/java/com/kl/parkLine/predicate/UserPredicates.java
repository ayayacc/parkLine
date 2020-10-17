package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QUser;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.vo.UserVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class UserPredicates
{
    public Predicate fuzzy(UserVo userVo, User user) 
    {
        QUser qUser = QUser.user;
        BooleanBuilder where = new BooleanBuilder();
        
        //姓名
        if (!StringUtils.isEmpty(userVo.getName()))
        {
            where.and(qUser.name.containsIgnoreCase(userVo.getName()));
        }
        
        //手机号
        if (!StringUtils.isEmpty(userVo.getMobile()))
        {
            where.and(qUser.mobile.containsIgnoreCase(userVo.getMobile()));
        }
        
        return where;
    }
}
