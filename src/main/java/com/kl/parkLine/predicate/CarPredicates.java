package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QCar;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.RoleType;
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
        where.and(userFilter(user));
        
        return where;
    }
    
    //角色过滤，公司类型的账户可以看所有车辆,其他用户只能看自己车辆
    private Predicate userFilter(User user) 
    {
        QCar qCar = QCar.car;
        BooleanBuilder where = new BooleanBuilder();
        
        //公司类型的账户可以看所有车辆
        if (user.hasRoleType(RoleType.company))
        {
            return where;
        }
        else //其他用户只能看自己车辆
        {
            where.and(qCar.user.eq(user));
        }
        
        return where;
    }
}
