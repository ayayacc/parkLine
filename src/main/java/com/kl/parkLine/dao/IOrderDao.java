package com.kl.parkLine.dao;

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
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.vo.OrderVo;

@Repository
public interface IOrderDao extends JpaRepository<Order, Integer>, QuerydslPredicateExecutor<OrderVo>
{
    public Order findOneByActId(String actId);
    public List<Order> findByTypeAndCarAndParkAndStatusOrderByStartDate(OrderType orderType, Car car, Park park, OrderStatus status);
    public Order findTopByTypeAndCarAndParkAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateDesc(OrderType type, Car car, Park park, OrderStatus status, Date endDate, Date startDate);
    public Order findTopByPlateIdOrderByInTimeDescCreatedDateDesc(Integer plateId);
    public Order findTopByOutDeviceSnAndIsOutIsFalseOrderByOutTimeDesc(String outDeviceSn);
    public Order findOneByOrderId(Integer orderId);
    public Order findOneByCode(String code);
    public Order findTopByCarAndTypeAndIsOutIsFalseOrderByInTimeDesc(Car car, OrderType type);
    public Boolean existsByTypeAndCarAndParkAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(OrderType type, Car car, Park park, List<OrderStatus> status, Date endDate, Date startDate);
    public Boolean existsByTypeAndCarAndStatus(OrderType type, Car car, OrderStatus status);
    public Set<Order> findByCarAndOwnerIsNull(Car car);
    public Page<OrderVo> findByTypeAndStatusAndOwnerOrderByCreatedDateDesc(OrderType orderType, OrderStatus status, User owner, Pageable pageable);
    public Integer countByOwnerAndTypeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(User owner, OrderType orderType, OrderStatus status, Date startDate, Date endDate);
    public List<Order> findByTypeAndStatusAndEndDateLessThan(OrderType orderType, OrderStatus status, Date date);
}