package com.kl.parkLine.predicate;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QCoupon;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.CouponVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class CouponPredicates
{
    public Predicate fuzzy(CouponVo couponVo, User user) 
    {
        QCoupon qCoupon = QCoupon.coupon;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(couponVo.getCode()))
        {
            where.and(qCoupon.code.containsIgnoreCase(couponVo.getCode()));
        }
        
        //名称
        if (!StringUtils.isEmpty(couponVo.getName()))
        {
            where.and(qCoupon.couponDef.name.containsIgnoreCase(couponVo.getName()));
        }
        
        //状态
        if (!StringUtils.isEmpty(couponVo.getStatus()))
        {
            where.and(qCoupon.status.eq(CouponStatus.valueOf(couponVo.getStatus())));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    /**
     * 区分用户权限
     * @param user
     * @return
     */
    private Predicate roleFilter(User user) 
    {
        //TODO: 角色过滤，普通用户只能看自己的优惠券
        QCoupon qCoupon = QCoupon.coupon;
        BooleanBuilder where = new BooleanBuilder();
        
        //普通用户只能看自己车辆
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qCoupon.owner.name.eq(user.getName()));
        }
        
        return where;
    }
}
