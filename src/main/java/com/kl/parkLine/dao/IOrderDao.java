package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Order;

@Repository(value="orderDao")
public interface IOrderDao extends JpaRepository<Order, Integer>
{
    public Order findOneByActId(String actId);
    public Order findOneByOrderId(Integer orderId);
}