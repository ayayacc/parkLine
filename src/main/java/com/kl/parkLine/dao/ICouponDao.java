package com.kl.parkLine.dao;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.CouponDef;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.vo.CouponVo;

@Repository
public interface ICouponDao extends JpaRepository<Coupon, Integer>, QuerydslPredicateExecutor<CouponVo>, JpaSpecificationExecutor<CouponVo>
{
    public Integer countByOwnerAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User owner, CouponStatus status, Date startDate, Date endDate);
    public boolean existsByCouponDefAndOwnerAndStatus(CouponDef couponDef, User owner, CouponStatus status);
    public Coupon findOneByCouponId(Integer couponDefId);
    public Page<CouponVo> findByOwnerAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualAndApplicableParksContainsOrApplicableParksIsNullOrderByDiscountAscEndDateAsc(User owner, CouponStatus status, Date startDate, Date endDate, Park park, Pageable pageable);
}