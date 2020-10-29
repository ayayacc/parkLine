package com.kl.parkLine.dao;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;

@Repository
public interface ICouponDao extends JpaRepository<Coupon, Integer>
{
    public boolean existsByCouponDefCouponDefId(Integer couponDefId);
    public Coupon findOneByCouponId(Integer couponDefId);
    public Coupon findTopByOwnerAndStatusAndMinAmtLessThanEqualOrderByAmtDescEndDate(User owner, CouponStatus status, BigDecimal minAmt);
}