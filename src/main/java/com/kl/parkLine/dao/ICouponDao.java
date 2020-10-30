package com.kl.parkLine.dao;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.vo.CouponVo;

@Repository
public interface ICouponDao extends JpaRepository<Coupon, Integer>, QuerydslPredicateExecutor<CouponVo>
{
    public boolean existsByCouponDefCouponDefId(Integer couponDefId);
    public Coupon findOneByCouponId(Integer couponDefId);
    public Coupon findTopByOwnerAndStatusAndMinAmtLessThanEqualOrderByAmtDescEndDate(User owner, CouponStatus status, BigDecimal minAmt);
    public Page<CouponVo> findByOwnerAndStatusAndMinAmtLessThanEqualOrderByAmtDescEndDate(User owner, CouponStatus status, BigDecimal minAmt, Pageable pageable);
}