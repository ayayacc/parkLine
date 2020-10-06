package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.CouponDef;

@Repository(value="couponDefDao")
public interface ICouponDefDao extends JpaRepository<CouponDef, Integer>
{
}