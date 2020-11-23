package com.kl.parkLine.predicate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.Order;
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
    //可以访问所有优惠券的角色
    private final List<String> allAccessRoleCodes = new ArrayList<String>();
    
    public CouponPredicates()
    {
        //系统管理员
        allAccessRoleCodes.add(RoleCode.SYS_ADMIN);
        //运营
        allAccessRoleCodes.add(RoleCode.BIZ_OPERATE);
    }
    
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
            where.and(qCoupon.status.eq(couponVo.getStatus()));
        }
        
        //区分用户权限
        where.and(userFilter(user));
        
        return where;
    }
    
    public Predicate applicable4Order(Order order) 
    {
        QCoupon qCoupon = QCoupon.coupon;
        BooleanBuilder where = new BooleanBuilder();
        Date today = new DateTime().withTimeAtStartOfDay().toDate();
        where.and(qCoupon.status.eq(CouponStatus.valid))
        .and(qCoupon.owner.eq(order.getOwner()))
        .and(qCoupon.startDate.loe(today))
        .and(qCoupon.endDate.goe(today))
        .and(qCoupon.applicableParks.contains(order.getPark()).or(qCoupon.applicableParks.isEmpty()));
        
        return where;
    }
    
    /**
     * 普通用户只能访问自己的优惠券
     * 产品运营，系统管理员和运营可以看所有优惠券,其他用户只能看自己的优惠券
     * @param user
     * @return
     */
    private Predicate userFilter(User user) 
    {
        //用户过滤，系统管理员和运营可以看所有优惠券,其他用户只能看自己的优惠券
        QCoupon qCoupon = QCoupon.coupon;
        BooleanBuilder where = new BooleanBuilder();
        
        //系统管理员和运营可以看所有优惠券
        if (user.hasAnyRole(allAccessRoleCodes))
        {
            return where;
        }
        //其他用户只能看自己的优惠券
        where.and(qCoupon.owner.name.equalsIgnoreCase(user.getName()));
        
        return where;
    }
}
