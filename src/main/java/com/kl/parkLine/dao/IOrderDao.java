package com.kl.parkLine.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Order;
import com.kl.parkLine.vo.OrderVo;

@Repository
public interface IOrderDao extends JpaRepository<Order, Integer>, QuerydslPredicateExecutor<OrderVo>
{
    public Order findOneByActId(String actId);
    public Order findOneByOrderId(Integer orderId);
    //public Page<OrderVo> findByStatusAndAmt
}