package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QRole;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.vo.RoleVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class RolePredicates
{
    public Predicate fuzzy(RoleVo roleVo, User user) 
    {
        QRole qRole = QRole.role;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(roleVo.getCode()))
        {
            where.and(qRole.code.containsIgnoreCase(roleVo.getCode()));
        }
        
        //名称
        if (!StringUtils.isEmpty(roleVo.getName()))
        {
            where.and(qRole.name.containsIgnoreCase(roleVo.getName()));
        }
        
        return where;
    }
}
