package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Coupon;

@Repository
public interface ICouponDao extends JpaRepository<Coupon, Integer>
{
    public boolean existsByCouponDefCouponDefId(Integer couponDefId);
    public Coupon findOneByCouponId(Integer couponDefId);
}