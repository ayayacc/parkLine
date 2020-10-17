package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QCar;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.CarVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class CarPredicates
{
    public Predicate fuzzy(CarVo carVo, User user) 
    {
        QCar qCar = QCar.car;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(carVo.getCarNo()))
        {
            where.and(qCar.carNo.containsIgnoreCase(carVo.getCarNo()));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    //角色过滤，普通用户只能看自己车辆
    private Predicate roleFilter(User user) 
    {
        QCar qCar = QCar.car;
        BooleanBuilder where = new BooleanBuilder();
        
        //普通用户只能看自己车辆
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qCar.user.name.eq(user.getName()));
        }
        
        return where;
    }
}
