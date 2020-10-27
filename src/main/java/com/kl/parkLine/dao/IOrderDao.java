package com.kl.parkLine.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.vo.OrderVo;

@Repository
public interface IOrderDao extends JpaRepository<Order, Integer>, QuerydslPredicateExecutor<OrderVo>
{
    public Order findOneByActId(String actId);
    public Order findOneByOrderId(Integer orderId);
    public Order findOneByCode(String code);
    public Boolean existsByTypeAndCarCarNoAndParkParkIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(OrderType type, String carNo, Integer parkId, List<OrderStatus> status, Date endDate, Date startDate);
    public Set<Order> findByCarAndOwnerIsNull(Car car);
    public Page<OrderVo> findByStatusAndOwnerAndAmtGreaterThan(OrderStatus status, User owner, BigDecimal amt, Pageable pageable);
}