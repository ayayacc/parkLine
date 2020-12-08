package com.kl.parkLine.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.events.EventException;

import com.kl.parkLine.component.AliYunCmpt;
import com.kl.parkLine.component.Utils;
import com.kl.parkLine.component.WxCmpt;
import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.dao.IParkStepFeeDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.OrderLog;
import com.kl.parkLine.entity.OrderPayment;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkFixedFee;
import com.kl.parkLine.entity.ParkStepFee;
import com.kl.parkLine.entity.QCar;
import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.QOrderPayment;
import com.kl.parkLine.entity.QPark;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CarType;
import com.kl.parkLine.enums.ChargeType;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.enums.DeviceUseage;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PaymentType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.ActiveCouponParam;
import com.kl.parkLine.json.ChargeWalletParam;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.json.MonthlyTktParam;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.TimePoint;
import com.kl.parkLine.json.WxPayNotifyParam;
import com.kl.parkLine.json.WxUnifiedOrderResult;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.OrderPaymentVo;
import com.kl.parkLine.vo.OrderVo;
import com.kl.parkLine.vo.ParkLocationVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;


/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderService
{
    @Autowired
    private IOrderDao orderDao;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private ParkService parkService;
    
    /*@Autowired
    private OrderPredicates orderPredicates;*/
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    @Autowired
    private IParkStepFeeDao parkStepFeeDao;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private AliYunCmpt aliYunOssCmpt;
    
    @Autowired
    private WxCmpt wxCmpt;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private ParkCarItemService parkCarItemService;
    
    private final List<OrderStatus> checkedStatus = new ArrayList<OrderStatus>();
    
    //已经完成的订单状态
    private final List<OrderStatus> completedStauts = new ArrayList<OrderStatus>();
    
    public OrderService()
    {
        //检查重复订单包含的状态: 存在等待付款和已经付款的月票订单时，不能再次创建重复的月票
        checkedStatus.add(OrderStatus.payed);
        
        //已经完成的订单状态：已经付款的或者无需支付的
        completedStauts.add(OrderStatus.payed);
        completedStauts.add(OrderStatus.noNeedToPay);
    }  
    
    /**
     * 分页查询
     * @param searchPred
     * @param pageable
     * @return
     */
    /*private Page<OrderVo> fuzzyFindPage(Predicate searchPred, Pageable pageable)
    {
        QOrder qOrder = QOrder.order;
        QPark qPark = QPark.park;
        QCar qCar = QCar.car;
        QueryResults<OrderVo> queryResults = jpaQueryFactory
                .select(Projections.constructor(OrderVo.class, qOrder.orderId,
                        qOrder.code,
                        qOrder.type,
                        qOrder.status,
                        qPark.parkId.as("parkParkId"),
                        qPark.name.as("parkName"),
                        qCar.carId.as("carCarId"),
                        qCar.carNo.as("carCarNo"),
                        qOrder.inTime,
                        qOrder.outTime,
                        qOrder.amt,
                        qOrder.realAmt,
                        qOrder.walletBalance,
                        qOrder.paymentTime,
                        qOrder.startDate,
                        qOrder.endDate,
                        qOrder.inImgCode,
                        qOrder.outImgCode))
                .from(qOrder).leftJoin(qPark).on(qOrder.park.eq(qPark))
                .leftJoin(qCar).on(qOrder.car.eq(qCar))
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
    
    *//**
     * 作为终端用户分页查询
     * @param orderVo
     * @param pageable
     * @param userName
     * @return
     *//*
    public Page<OrderVo> fuzzyFindPageAsUser(OrderVo orderVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        
        Predicate searchPred = orderPredicates.fuzzyAsEndUser(orderVo, user);
        
        return fuzzyFindPage(searchPred, pageable);
    }
    
    *//**
     * 作为后台管理(停车场/管理员)分页查询
     * @param orderVo
     * @param pageable
     * @param userName
     * @return
     *//*
    public Page<OrderVo> fuzzyFindPageAsManager(OrderVo orderVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        
        Predicate searchPred = orderPredicates.fuzzyAsManager(orderVo, user);
        
        return fuzzyFindPage(searchPred, pageable);
    }*/
    
    public Order findOneByOrderId(Integer orderId) 
    {
        return orderDao.findOneByOrderId(orderId);
    }
    
    /**
     * 判断订单车辆是否到达道闸
     */
    private Boolean arrivedGate(Order order)
    {
        for (OrderLog log : order.getLogs())
        {
            if (log.getEvent().getType().equals(EventType.out))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 根据最近的车辆未出场的订单
     * @param car
     * @return
     * @throws BusinessException 
     * @throws ParseException 
     */
    public OrderVo findParkingByCar(Car car) throws ParseException, BusinessException
    {
        //找到最近的车辆在场订单
        Order order = orderDao.findTopByCarAndTypeAndIsOutIsFalseOrderByInTimeDesc(car, OrderType.parking);
        
        OrderVo orderVo = null;
        if (null == order) //无车辆在场订单,返回空
        {
            return null;
        }
        else 
        {
            if (!arrivedGate(order)) //车辆未到达道闸,提前支付
            {
                setAmtAndOutTimeLimit(order);
                save(order);
            }
            orderVo = OrderVo.builder().orderId(order.getOrderId())
                    .code(order.getCode())
                    .amt(order.getAmt())
                    .carCarId(order.getCar().getCarId())
                    .carCarNo(order.getCar().getCarNo())
                    .lastPaymentTime(order.getLastPaymentTime())
                    .outTimeLimit(order.getOutTimeLimit())
                    .parkName(order.getPark().getName())
                    .parkParkId(order.getPark().getParkId())
                    .payedAmt(order.getPayedAmt())
                    .realPayedAmt(order.getRealPayedAmt())
                    .realUnpayedAmt(order.getRealUnpayedAmt())
                    .inTime(order.getInTime())
                    .status(order.getStatus())
                    .build();
        }
        return orderVo;
    }
    
    public ParkLocationVo findParkLocationByCar(Car car) throws BusinessException 
    {
        Order order = orderDao.findTopByCarAndTypeAndIsOutIsFalseOrderByInTimeDesc(car, OrderType.parking);
        if (null == order)
        {
            throw new BusinessException("未找到车辆的入场记录");
        }
        
        Park park = order.getPark();
        ParkLocationVo parkLocationVo = ParkLocationVo.builder()
                .parkId(park.getParkId())
                .code(park.getCode())
                .name(park.getName())
                .totalCnt(park.getTotalCnt())
                .availableCnt(park.getAvailableCnt())
                .lng(park.getGeo().getX())
                .lat(park.getGeo().getY())
                .contact(park.getContact())
                .build();
        return parkLocationVo;
    }
    
    /**
     * 根据设备序列号找到最近识别的订单
     * @param outDeviceSn
     * @return
     */
    public Order findLastNotOutByOutDeviceSn(String outDeviceSn) 
    {
        return orderDao.findTopByOutDeviceSnAndIsOutIsFalseOrderByOutTimeDesc(outDeviceSn);
    }
    
    /**
     * 保存一个订单
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    public void save(Order order) throws BusinessException
    {
        this.save(order, null);
    }
    
    /**
     * 保存一个订单
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    public void save(Order order, Event event) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == order.getOrderId()) //新增数据
        {
            order.setLogs(new ArrayList<OrderLog>());
        }
        else//编辑已有数据
        {
            //编辑订单，//合并字段
            Optional<Order> orderDst = orderDao.findById(order.getOrderId());
            
            if (false == orderDst.isPresent())
            {
                throw new BusinessException(String.format("无效的订单 Id: %d", order.getOrderId()));
            }
            
            //记录不同点
            diff = util.difference(orderDst.get(), order);
            
            BeanUtils.copyProperties(order, orderDst.get(), util.getNullPropertyNames(order));
            
            order = orderDst.get();
        }
        
        //保存数据
        OrderLog log = new OrderLog();
        log.setDiff(diff);
        log.setRemark(order.getChangeRemark().toString());
        log.setEvent(event);
        if (!StringUtils.isEmpty(diff)  //至少有一项内容时才添加日志
            || !StringUtils.isEmpty(order.getChangeRemark())
            || null != event)
        {
            log.setOrder(order);
            order.getLogs().add(log);
        }
        orderDao.save(order);
    }
    
    /**
     * 处理出入场/停车完成事件
     * @param event 事件对象
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ParseException 
     * @throws BusinessException 
     * @throws EventException 
     */
    public EventResult processEvent(Event event) throws BusinessException, NoSuchFieldException, ParseException
    {
        EventResult eventResult = null;
        switch (event.getType())
        {
            case in:  //入场事件,创建订单
                eventResult = carIn(event);
                break;
            case complete: //停车完成,订单计费,完成订单
                eventResult = carComplete(event);
                break;
            case cancel:
                eventResult = eventCancel(event);
                break;
            default:
                break;
        }
        
        return eventResult;
    }
    
    /**
     * 处理道闸设备轮询
     * @param deviceSn 道闸设备编号
     * @return
     * @throws BusinessException 
     */
    public EventResult processComet(String deviceSn) throws BusinessException
    {
        Date now = new Date();
        EventResult eventResult = EventResult.notOpen();
        Order order = findLastNotOutByOutDeviceSn(deviceSn); //找到最近的车辆未开闸出场订单
        if (null == order) //未找到订单，不开闸
        {
            return EventResult.notOpen();
        }
        else 
        {
            if (order.getStatus().equals(OrderStatus.noNeedToPay)) //无需付款，开闸
            {
                eventResult = EventResult.open(String.format("一路顺风:%s", order.getCar().getCarNo()));
            }
            else if (order.getStatus().equals(OrderStatus.payed)) //已经支付
            {
                //未超过出场时限
                if (!now.after(order.getOutTimeLimit()))
                {
                    eventResult = EventResult.open(String.format("已支付, 一路顺风:%s", order.getCar().getCarNo()));
                }
            }
        }
        
        //如果开闸
        if (eventResult.getOpen())
        {
            //停车场空位+1
            Park park = order.getPark();
            Integer newAvailableCnt = park.getAvailableCnt() + 1;
            park.setChangeRemark(String.format("停车完成, 停车场可用车位变化: %d --> %d", 
                    park.getAvailableCnt(), newAvailableCnt));
            park.setAvailableCnt(newAvailableCnt);
            parkService.save(park);
            
            //订单已经出场
            order.setIsOut(true);
            //保存
            this.save(order);
        }
        return eventResult;
    }
    
    
    /**
     * 停车入场事件处理
     * @param event 事件对象
     * @throws BusinessException 
     * @throws EventException 
     */
    public EventResult carIn(Event event) throws BusinessException 
    {
        //停车场
        Park park = null;
        if (null != event.getParkCode())
        {
            park = parkService.findOneByCode(event.getParkCode());
        }
        else
        {
            Device device = deviceService.findOneBySerialNo(event.getDeviceSn());
            park = device.getPark();
        }
        
        //车辆信息
        Car car = carService.getCar(event.getPlateNo(), event.getPlateColor());
        
        //检查是否在黑名单
        if (parkCarItemService.existsInBlackList(park, car))
        {
            return EventResult.notOpen(String.format("%s 黑名单车辆", event.getPlateNo()));
        }
        //检查车辆是否存在待交费订单
        if (park.getIsForbidenOwe()
                &&orderDao.existsByTypeAndCarAndStatus(OrderType.parking, car, OrderStatus.needToPay))
        {
            return EventResult.notOpen(String.format("%s 欠费车辆", event.getPlateNo()));
        }
        //停车场无空位
        if (0 >= park.getAvailableCnt())
        {
            return EventResult.notOpen("车位已满");
        }
        
        Order order = Order.builder()
                .code(util.makeCode(OrderType.parking))
                .car(car)
                .type(OrderType.parking)
                .owner(car.getUser())
                .status(OrderStatus.in)
                .park(park)
                .inDeviceSn(event.getDeviceSn())
                .actId(event.getActId())
                .plateId(event.getPlateId())
                .inTime(event.getTimeIn())
                .inImgCode(event.getPicUrlIn())
                .isOut(false)
                .build();
        //停车场空位-1
        Integer newAvailableCnt = park.getAvailableCnt() - 1;
        park.setChangeRemark(String.format("车辆入场, 停车场可用车位变化: %d --> %d, 事件: %s", 
                park.getAvailableCnt(), newAvailableCnt, event.getGuid()));
        park.setAvailableCnt(newAvailableCnt);
        parkService.save(park);
        
        //保存 订单
        this.save(order, event);
        return EventResult.open(String.format("欢迎光临:%s", event.getPlateNo()));
    }
    
    /**
     * 停车完成事件处理
     * @param event 事件对象
     * @throws BusinessException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ParseException 
     */
    public EventResult carComplete(Event event) throws BusinessException, NoSuchFieldException, SecurityException, ParseException
    {
        Date now = new Date();
        EventResult eventResult = new EventResult();
        Order order = null;
        if (null != event.getActId()) //有事件Id，高位摄像头停车场
        {
            //根据事件Id找入场时生成的的订单
            order = orderDao.findOneByActId(event.getActId());
            order.setIsOut(true); //高位摄像头直接出场
        }
        else //无事件Id，道闸停车场
        {
            //找到最近的入场订单
            order = orderDao.findTopByPlateIdAndStatusOrderByInTimeDesc(event.getPlateId(), OrderStatus.in);
        }
        
        if (null == order) //无入场记录,开闸
        {
            return EventResult.open(String.format("%s无入场", event.getPlateNo())) ;
        }

        //设置出场抓拍设备
        order.setOutDeviceSn(event.getDeviceSn());
        
        //出场截图
        order.setOutImgCode(event.getPicUrlOut());
        
        //设置出场时间
        order.setOutTime(event.getTimeOut());
        
        //订单未支付
        if (!order.getStatus().equals(OrderStatus.payed))
        {
            //计算金额以及设置出场限制
            this.setAmtAndOutTimeLimit(order);
            
            //如果订单价格是0，则直接变成无需支付状态
            if (order.getAmt().equals(BigDecimal.ZERO))  //无需付款,直接开闸
            {
                order.setStatus(OrderStatus.noNeedToPay);
                eventResult = EventResult.open(String.format("一路顺风:%s", event.getPlateNo()));
            }
            else  //产生费用
            {
                DateTime inTime = new DateTime(order.getInTime());
                DateTime outTime = new DateTime(order.getOutTime());
                Period period = new Period(inTime, outTime, PeriodType.time());
                //用户开通了无感支付
                if (null!=order.getOwner() && order.getOwner().getIsQuickPay())
                {
                    try
                    {
                        this.quickPayByWallet(order); //无感支付, 钱包支付订单
                        eventResult = EventResult.open(String.format("停车时长%d小时%d分，无感支付%.2f元", 
                                period.getHours(), period.getMinutes(), order.getAmt().floatValue()));
                    }
                    catch (Exception e)
                    {
                        OrderLog log = OrderLog.builder().order(order).build();
                        log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                        order.getLogs().add(log);
                        order.setStatus(OrderStatus.needToPay);
                        eventResult = EventResult.notOpen(String.format("停车时长%d小时%d分，请交费%.2f元", 
                                period.getHours(), period.getMinutes(), order.getAmt().floatValue()));
                    }
                }
                else //用户未开通无感支付
                {
                    order.setStatus(OrderStatus.needToPay);
                    eventResult = EventResult.notOpen(String.format("停车时长%d小时%d分,请交费%.2f元", 
                            period.getHours(), period.getMinutes(), order.getAmt().floatValue()));
                }
            }
        }
        else //订单已经支付
        {
            //检查是否已经超过出场限制
            if (!now.after(order.getOutTimeLimit())) //未超过时间限制
            {
                eventResult = EventResult.open(String.format("已支付，一路顺风:%s", event.getPlateNo()));
            }
            else //超过离场时间限制
            {
                DateTime startTime = new DateTime(order.getOutTimeLimit());
                DateTime endTime = new DateTime(now);
                Period period = new Period(startTime, endTime, PeriodType.time());
                //计算需要补缴的费用
                resetAmtAndOutTimeLimit(order);
                if (order.getStatus().equals(OrderStatus.needToPay))  //产生新的费用
                {
                    BigDecimal unPayedAmt = order.getAmt().subtract(order.getPayedAmt());
                    //用户开通了无感支付
                    if (order.getOwner().getIsQuickPay())
                    {
                        try
                        {
                            this.quickPayByWallet(order); //无感支付, 钱包支付订单
                            eventResult = EventResult.open(String.format("超时%d小时%d分, 无感支付%.2f元", 
                                    period.getHours(), period.getMinutes(), unPayedAmt.floatValue()));
                        }
                        catch (Exception e)
                        {
                            OrderLog log = OrderLog.builder().order(order).build();
                            log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                            order.getLogs().add(log);
                            order.setStatus(OrderStatus.needToPay);
                            eventResult = EventResult.notOpen(String.format("停车时长%d小时%d分, 请补交费%.2f元", 
                                    period.getHours(), period.getMinutes(), unPayedAmt.floatValue()));
                        }
                    }
                    else //用户未开通无感支付
                    {
                        order.setStatus(OrderStatus.needToPay);
                        eventResult = EventResult.notOpen(String.format("超时%d小时%d分,请补交费%.2f元", 
                                period.getHours(), period.getMinutes(), unPayedAmt.floatValue()));
                    }
                }
                else  //超时未产生新的费用
                {
                    eventResult = EventResult.open(String.format("已支付，一路顺风:%s", event.getPlateNo()));
                }
            }
        }
        
        //如果开闸
        if (eventResult.getOpen())
        {
            //停车场空位+1
            Park park = order.getPark();
            Integer newAvailableCnt = park.getAvailableCnt() + 1;
            park.setChangeRemark(String.format("停车完成, 停车场可用车位变化: %d --> %d, 事件: %s", 
                    park.getAvailableCnt(), newAvailableCnt, event.getGuid()));
            park.setAvailableCnt(newAvailableCnt);
            parkService.save(park);
            
            //订单已经出场
            order.setIsOut(true);
        }

        //保存
        this.save(order, event);
        
        return eventResult;
    }
    
    /**
     * 事件取消事件（人工清理时触发）
     * @param event
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public EventResult eventCancel(Event event) throws BusinessException, NoSuchFieldException, SecurityException
    {
        //涉及到的订单
        Order order = orderDao.findOneByActId(event.getActId());
        if (null == order)
        {
            return EventResult.notOpen(String.format("无效事件Id: %s", event.getActId()));
        }
        
        //取消targetEvent
        Park park = order.getPark();
        Event targetEvent = eventService.findOneByGuidAndParkCode(event.getTargetGuid(), park.getCode());
        if (null == targetEvent) //未找到被取消的事件
        {
            return EventResult.notOpen(String.format("无效事件Id: %s", event.getActId()));
        }
        
        //如果订单已经付款以及后续状态，返回失败
        if (OrderStatus.payed.getValue() <= order.getStatus().getValue())
        {
            String msg = String.format("停车订单【%s】处于【%s】状态, 无法撤销", 
                    order.getCode(), order.getStatus().getText());
            event.setRemark(msg);
            return EventResult.notOpen(msg);
        }
        
        // 取消的是入场事件，取消订单
        if (EventType.in.getValue() == event.getTargetType().getValue())
        {
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //当前订单状态是入场，停车场空位数量+1
            if (OrderStatus.in.getValue() == order.getStatus().getValue())
            {
                Integer newAvailableCnt = park.getAvailableCnt() + 1;
                park.setChangeRemark(String.format("取消车辆入场, 停车场可用车位变化: %d --> %d, 事件: %s, 被取消事件: %s", 
                        park.getAvailableCnt(), newAvailableCnt, 
                        event.getGuid(), targetEvent.getGuid()));
                park.setAvailableCnt(newAvailableCnt);
                parkService.save(park);
            }
            
            //取消订单
            order.setStatus(OrderStatus.canceled);
        }
        // 取消的是停车完成事件
        else if(EventType.complete.getValue() == event.getTargetType().getValue())
        {
            //将订单改成入场状态
            order.setStatus(OrderStatus.in);
            order.setIsOut(false);
            
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //出场时间为无效时间
            order.setOutTime(null);
            
            //停车场空位数量-1
            Integer newAvailableCnt = park.getAvailableCnt() - 1;
            park.setChangeRemark(String.format("取消车停车完成, 停车场可用车位变化: %d --> %d, 事件: %s, 被取消事件: %s", 
                    park.getAvailableCnt(), newAvailableCnt, 
                    event.getGuid(), targetEvent.getGuid()));
            park.setAvailableCnt(newAvailableCnt);
            parkService.save(park);
            park.setAvailableCnt(newAvailableCnt);
        }
        
        //保存订单
        this.save(order, event);
        
        //禁用目标事件
        targetEvent.setEnabled("N");
        eventService.save(targetEvent);
        return EventResult.open("处理成功");
    }
    
    /**
     * 计算订单的停车时长，扣除月票时间
     * @param order
     * @return
     */
    public Integer getParkingMinutes(Order order, DateTime outTime)
    {
        //月票
        //找到车子在该停车场的有效月票
        List<Order> monthlyTkts = orderDao.findByTypeAndCarAndParkAndStatusOrderByStartDate(OrderType.monthlyTicket, order.getCar(), order.getPark(), OrderStatus.payed);
        List<TimePoint> timePoints = new ArrayList<>();
        for (Order monthlyTkt : monthlyTkts)
        {
            TimePoint startPoint = new TimePoint();
            //start point
            startPoint.setDateTime(new DateTime(monthlyTkt.getStartDate()));
            startPoint.setKey("startDate");
            startPoint.setOrder(monthlyTkt);
            timePoints.add(startPoint);
            
            TimePoint endPoint = new TimePoint();
            endPoint.setDateTime(new DateTime(monthlyTkt.getEndDate()).plusDays(1)); //第二天的凌晨
            endPoint.setKey("endDate");
            endPoint.setOrder(monthlyTkt);
            timePoints.add(endPoint);
        }
        
        //入场时间
        TimePoint inPoint = new TimePoint();
        inPoint.setDateTime(new DateTime(order.getInTime()));
        inPoint.setKey("inTime");
        timePoints.add(inPoint);
        
        //出场时间
        TimePoint outPoint = new TimePoint();
        outPoint.setDateTime(new DateTime(outTime));
        outPoint.setKey("outTime");
        timePoints.add(outPoint);
        
        //按时间排序
        Collections.sort(timePoints);
        Integer minutes = 0;
        Boolean covering = false;  //是否被月票覆盖中
        TimePoint startPoint = null;
        for (TimePoint currentPoint : timePoints)
        {
            if (currentPoint.getKey().equals("inTime"))  //入场点
            {
                if (!covering) //如果没有被月票覆盖，开始计时
                {
                    startPoint = currentPoint;
                }
            }
            else if (currentPoint.getKey().equals("startDate")) //月票起始点
            {
                covering = true;  //开始被月票覆盖，累计开始月票前的停车时长
                order.setUsedMonthlyTkt(currentPoint.getOrder());
                if (null != startPoint)
                {
                    minutes += Minutes.minutesBetween(startPoint.getDateTime(), currentPoint.getDateTime()).getMinutes();
                }
            }
            else if (currentPoint.getKey().equals("endDate")) //月票终止点
            {
                covering = false;  //结束被月票覆盖，设置计时开始点
                startPoint = currentPoint;
                continue;
            }
            else if (currentPoint.getKey().equals("outTime")) //出场点
            {
                if (!covering)
                {
                    minutes += Minutes.minutesBetween(startPoint.getDateTime(), currentPoint.getDateTime()).getMinutes();
                }
                break;
            }
        }
        
        return minutes;
    }
    
    /**
     * 计算订单金额，并且设置到order中
     * @param park
     * @param order
     * @return
     * @throws ParseException 
     * @throws BusinessException 
     */
    public void setAmtAndOutTimeLimit(Order order) throws ParseException, BusinessException
    {
        Park park = order.getPark();
        Car car = order.getCar();
        
        //白牌车免费
        if (park.getIsWhitePlateFree() && car.getPlateColor().equals(PlateColor.white))
        {
            order.setAmt(BigDecimal.ZERO);
            order.setRealUnpayedAmt(BigDecimal.ZERO);
            return;
        }
        //白名单免费
        if (parkCarItemService.existsInWhiteList(park, car))
        {
            order.setAmt(BigDecimal.ZERO);
            order.setRealUnpayedAmt(BigDecimal.ZERO);
            return;
        }
        
        //出入场时间
        BigDecimal amt = BigDecimal.ZERO;
        DateTime outTime = null;
        if (null == order.getOutTime()) //提前支付
        {
            outTime = new DateTime();
        }
        else
        {
            outTime = new DateTime(order.getOutTime());
        }
        //设置出场时间限制,30分钟,设置出场时间限制必须同时计算价格
        DateTime outTimeLimit = outTime.plusMinutes(Const.OUT_LIMIT_TIME_NIN);
        order.setOutTimeLimit(outTimeLimit.toDate());
        
        //结合月票，计算停车时长
        Integer minutes = this.getParkingMinutes(order, outTime);
        
        //阶梯计费
        if (park.getChargeType().equals(ChargeType.step))
        {
            ParkStepFee parkStepFee = parkStepFeeDao.findOneByParkAndCarTypeAndStartMinLessThanEqualAndEndMinGreaterThan(
                    park, car.getCarType(), minutes, minutes);
            if (null == parkStepFee)
            {
                throw new BusinessException(String.format("停车场  %s 阶梯费用配置错误, 缺少: %s 车  %d 分钟收费配置", 
                        park.getName(), car.getCarType().getText(), minutes));
            }
            amt = parkStepFee.getAmt();
        }
        //固定费率
        else if (park.getChargeType().equals(ChargeType.fixed))
        {
            ParkFixedFee parkFixedFee = null;
            if (car.getCarType().equals(CarType.newEnergy)) //新能源计费规则
            {
                parkFixedFee = park.getNewEnergyFixedFee();
            }
            else  //燃油车计费规则(默认)
            {
                parkFixedFee = park.getFuelFixedFee();
            }
            
            //封顶次数
            int maxCnt = minutes / (parkFixedFee.getMaxPeriod()*60); //最大计费周期以小时为单位
            amt = parkFixedFee.getMaxAmt().multiply(new BigDecimal(maxCnt));
            
            //未满封顶的时间
            int modMinutes = minutes % (parkFixedFee.getMaxPeriod()*60);
            //免费时长
            BigDecimal feeTimeTotal = new BigDecimal(modMinutes - parkFixedFee.getFreeTime()); //计算应该计费的时间
            if (0 < feeTimeTotal.compareTo(BigDecimal.ZERO)) //超过免费时间(超过x分钟收费)
            {
                BigDecimal modAmt = feeTimeTotal.divide(new BigDecimal(parkFixedFee.getFeePeriod()), 
                        RoundingMode.CEILING).multiply(parkFixedFee.getPrice());
                modAmt = modAmt.min(parkFixedFee.getMaxAmt());
                amt = amt.add(modAmt);
            }
        }
        order.setAmt(amt);
        //提前交费超时后，补缴费
        if (order.getStatus().equals(OrderStatus.payed))
        {
            //realAmt = amt-(payedAmt-realPayed)
            order.setRealUnpayedAmt(amt.subtract(order.getPayedAmt().subtract(order.getRealPayedAmt())));
        }
    }
    
    /**
     * 找到需要支付的停车订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myNeedToPayParking(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwnerOrderByCreatedDateDesc(OrderType.parking, OrderStatus.needToPay, user, pageable);
    }
    
    /**
     * 找到已经完成支付的停车订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myPayedParking(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwnerOrderByCreatedDateDesc(OrderType.parking, OrderStatus.payed, user, pageable);
    }
    
    /**
     * 我的钱包变动记录
     * @param userName
     * @return
     */
    public Page<OrderPaymentVo> myWalletLogs(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        QOrderPayment qOrderPayment = QOrderPayment.orderPayment;
        QOrder qOrder = QOrder.order;
        QPark qPark = QPark.park;
        QCar qCar = QCar.car;
        QueryResults<OrderPaymentVo> queryResults = jpaQueryFactory
                .select(Projections.constructor(OrderPaymentVo.class, qOrderPayment.orderPaymentId,
                        qOrder.orderId.as("orderOrderId"),
                        qOrder.code.as("orderCode"),
                        qOrder.type.as("orderType"),
                        qOrder.status.as("orderStatus"),
                        qPark.parkId.as("orderParkParkId"),
                        qPark.name.as("orderParkName"),
                        qCar.carId.as("orderCarCarId"),
                        qCar.carNo.as("orderCarCarNo"),
                        qOrder.inTime.as("orderInTime"),
                        qOrder.outTime.as("orderOutTime"),
                        qOrder.amt.as("orderAmt"),
                        qOrderPayment.amt.as("payedAmt"),
                        qOrderPayment.walletBalance,
                        qOrderPayment.paymentTime,
                        qOrder.startDate.as("orderStartDate"),
                        qOrder.endDate.as("orderEndDate"),
                        qOrder.inImgCode.as("orderInImgCode"),
                        qOrder.outImgCode.as("orderOutImgCode")))
                .from(qOrderPayment).leftJoin(qOrder).on(qOrderPayment.order.eq(qOrder))
                .leftJoin(qPark).on(qOrder.park.eq(qPark))
                .leftJoin(qCar).on(qOrder.car.eq(qCar))
                .where(qOrder.owner.eq(user).and(qOrderPayment.walletBalance.isNotNull()))
                .orderBy(qOrderPayment.paymentTime.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
    
    /**
     * 找到已经完成支付的月票订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myPayedMonthlyTkt(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwnerOrderByCreatedDateDesc(OrderType.monthlyTicket, OrderStatus.payed, user, pageable);
    }
    
    /**
     * 找到等待付款的月票订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myNeedToPayMonthlyTkt(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwnerOrderByCreatedDateDesc(OrderType.monthlyTicket, OrderStatus.needToPay, user, pageable);
    }
    
    /**
     * 找到指定用户可以开票的订单
     * @param userName
     * @return
     */
    public Page<OrderVo> invoiceable(String userName, Pageable pageable)
    {
        //TODO: 查找可开票付款记录
        return null;
        //User user = userService.findByName(userName);
        //return orderDao.findByStatusAndOwnerAndAmtGreaterThanAndInvoiceIsNull(OrderStatus.payed, user, BigDecimal.ZERO, pageable);
    }
    
    /**
     * 将车辆涉及的无主订单设置拥有者
     * @param car 被绑定用户的车辆
     * @throws BusinessException 
     */
    public void setOrderOwnerByCar(Car car) throws BusinessException 
    {
        //找到指定车牌号的无主订单
        Set<Order> orders = orderDao.findByCarAndOwnerIsNull(car);
        
        //设置拥有者
        for (Order order : orders)
        {
            order.setOwner(car.getUser());
            order.appedChangeRemark(String.format("随车辆绑定到用户: %s", car.getUser().getName()));
            this.save(order);
        }
    }
    
    /**
     * 处理微信支付成功结果
     * @param wxPayNotifyParam
     */
    public void wxPaySuccess(WxPayNotifyParam wxPayNotifyParam) throws BusinessException
    {
        //找到付款订单
        Order order = orderDao.findOneByCode(wxPayNotifyParam.getOutTradeNo());
        if (null == order)
        {
            return;
        }
        if (!order.getStatus().equals(OrderStatus.needToPay))  //已经处理过付款通知(微信会重复推送同一张订单的付款通知)
        {
            return;
        }
        //修改订单状态
        order.setStatus(OrderStatus.payed);
        //微信订单号
        order.setWxTransactionId(wxPayNotifyParam.getTransactionId());
        //设置用户关注公众号情况
        order.getOwner().setSubscribe(wxPayNotifyParam.getIsSubscribe());
        
        //设置订单支付
        order.setStatus(OrderStatus.payed);
        order.setPayedAmt(order.getAmt());
        BigDecimal realPayedAmt = order.getRealPayedAmt();
        if (null == realPayedAmt)
        {
            realPayedAmt = BigDecimal.ZERO;
        }
        order.setRealPayedAmt(realPayedAmt.add(order.getRealUnpayedAmt()));
        order.setRealUnpayedAmt(BigDecimal.ZERO);
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setRealAmt(order.getRealUnpayedAmt());
        orderPayment.setAmt(order.getAmt());
        orderPayment.setBankType(wxPayNotifyParam.getBankType());
        orderPayment.setOrder(order);
        orderPayment.setPaymentType(PaymentType.wx);
        orderPayment.setPaymentTime(wxPayNotifyParam.getTimeEnd());
        order.setLastPaymentTime(wxPayNotifyParam.getTimeEnd());
        order.getOrderPayments().add(orderPayment);
        
        switch (order.getType())
        {
            case walletIn: //钱包充值订单:增加钱包余额
                User owner = order.getOwner();
                owner.setBalance(owner.getBalance().add(order.getAmt()));
                orderPayment.setWalletBalance(owner.getBalance());//记录钱包余额
                break;
            case coupon:
                Coupon coupon = order.getActivatedCoupon();
                coupon.setStatus(CouponStatus.valid);
                //默认激活七天
                DateTime now = new DateTime();
                coupon.setStartDate(now.toDate());
                coupon.setEndDate(now.plusDays(Const.COUPON_ACTIVE_DAYS).toDate());
                break;
            default:
                break;
        }
        
        order.appedChangeRemark(String.format("微信付款成功: %s", wxPayNotifyParam));
        this.save(order);
    }
    
    /**
     * 处理订单支付失败结果
     * @param wxPayNotifyParam
     * @throws BusinessException 
     */
    public void wxPayFail(WxPayNotifyParam wxPayNotifyParam) throws BusinessException
    {
      //找到付款订单
        Order order = orderDao.findOneByCode(wxPayNotifyParam.getOutTradeNo());
        if (null == order)
        {
            return;
        }
        if (order.getStatus().equals(OrderStatus.needToPay))  //已经处理过付款通知(微信会重复推送同一张订单的付款通知)
        {
            return;
        }
        //修改订单状态
        order.setStatus(OrderStatus.needToPay);
        //设置支付日期
        order.appedChangeRemark(String.format("微信付款失败: %s", wxPayNotifyParam));
        this.save(order);
    }

    /**
     * 检查月票订单参数有效性,开始结束日期是否在月头月尾,价格是否正确
     * @param park
     * @return
     */
    private void checkMonthlyTktDate(Date startDate, Date endDate) throws BusinessException
    {
        DateTime dateTimeStart = new DateTime(startDate);
        DateTime dateTimeEnd = new DateTime(endDate);
        DateTime today = new DateTime().withTimeAtStartOfDay();
        
        //开始日期不能早于今天
        if (dateTimeStart.isBefore(today))
        {
            throw new BusinessException("开始日期不能早于今天");
        }
        
        //检查日期大小
        if (dateTimeStart.isAfter(dateTimeEnd))
        {
            throw new BusinessException("开始日期必须小于结束日期");
        }
    }
    
    //检查是否存在重复的月票订单:车辆,停车场,时间段,状态：已经支付/待支付
    private Boolean existsValidMonthlyTkt(Car car, Park park, Date startDate, Date endDate)
    {
        return orderDao.existsByTypeAndCarAndParkAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual
                (OrderType.monthlyTicket, car, park, checkedStatus, endDate, startDate);
    }
    
    /**
     * 计算月票价格
     * @param park 停车场
     * @param car 车辆
     * @param dateTimeStart 开始时间
     * @param dateTimeEnd 结束时间
     * @return
     */
    private BigDecimal calMonthlyTktAmt(Park park, Car car, Date startDate, Date endDate)
    {
        BigDecimal amt = BigDecimal.ZERO;
        BigDecimal price = park.getFuelMonthlyPrice(); //默认按照燃油车计费
        if (car.getCarType().equals(CarType.newEnergy))
        {
            price = park.getNewEnergyMonthlyPrice();
        }
        
        //开始结束时间
        DateTime dateTimeStart = new DateTime(startDate);
        DateTime dateTimeEnd = new DateTime(endDate);
        
        //取开始时间到月末最后一天计算
        DateTime theEndDataOfMonth = dateTimeStart.dayOfMonth().withMaximumValue();
        while (dateTimeEnd.isAfter(theEndDataOfMonth))
        {
            BigDecimal days = new BigDecimal(Days.daysBetween(dateTimeStart, theEndDataOfMonth).getDays() + 1); //包含当天
            amt = amt.add(price.multiply(days).divide(new BigDecimal(dateTimeStart.dayOfMonth().getMaximumValue()), 2,RoundingMode.HALF_UP));
            
            //下个月第一天
            dateTimeStart = theEndDataOfMonth.plusDays(1);
            theEndDataOfMonth = dateTimeStart.dayOfMonth().withMaximumValue();
        }
        
        //计算未到月末部分
        BigDecimal days = new BigDecimal(Days.daysBetween(dateTimeStart, dateTimeEnd).getDays() + 1); //包含当天
        amt = amt.add(price.multiply(days).divide(new BigDecimal(dateTimeStart.dayOfMonth().getMaximumValue()), 2,RoundingMode.HALF_UP));
        return amt;
    }
    
    /**
     * 月票询价，月票固定价格，根据此价格，计算每月的日停车单价，（不同月份的包日停车单价不同），月票价格分月，分段计算
     * @param monthlyTktParam
     * @throws Exception 
     */
    public BigDecimal inqueryMonthlyTkt(MonthlyTktParam monthlyTktParam) throws Exception
    {
        //找到停车场
        Park park = parkService.findOneById(monthlyTktParam.getParkId());
        if (null == park)
        {
            throw new BusinessException(String.format("无效的停车场Id: %d", monthlyTktParam.getParkId()));
        }
        
        //找到车辆
        Car car = carService.findOneById(monthlyTktParam.getCarId());
        if (null == car)
        {
            throw new BusinessException(String.format("无效的车辆Id: %d", monthlyTktParam.getCarId()));
        }
        
        checkMonthlyTktDate(monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
        
        //检查是否有重复的月票订单
        if (this.existsValidMonthlyTkt(car, park, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate()))
        {
            throw new BusinessException("请勿重复购买月票");
        }
        
        return calMonthlyTktAmt(park, car, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
    }    
    
    /**
     * 
     * @param payOrderParam
     * @return
     * @throws Exception
     */
    public BigDecimal inqueryParking(PayOrderParam payOrderParam, String payerName) throws BusinessException
    {
        //找到对应订单
        Order order = this.findOneByOrderId(payOrderParam.getOrderId());
        if (null == order)
        {
            throw new BusinessException(String.format("无效的订单Id: %d", payOrderParam.getOrderId()));
        }
        
        User payer = userService.findByName(payerName);
        if (null == payer)
        {
            throw new BusinessException(String.format("无效的用户: %d", payerName));
        }
        
        //找到对应优惠券
        Coupon coupon = couponService.findOneById(payOrderParam.getCouponId());
        if (null == coupon)
        {
            throw new BusinessException(String.format("无效的优惠券Id: %d", payOrderParam.getCouponId()));
        }
        
        //检查优惠券可用性
        checkCoupon(order, coupon, payer);
        
        //返回真实值
        return calRealAmt(order, coupon);
    }
    
    /**
     * 创建一个新的月票订单
     * @param monthlyTktParam
     * @throws Exception 
     */
    public OrderVo createMonthlyTkt(MonthlyTktParam monthlyTktParam, String userName) throws Exception
    {
        User owner = userService.findByName(userName);
        Park park = parkService.findOneById(monthlyTktParam.getParkId());
        
        //找到车辆
        Car car = carService.findOneById(monthlyTktParam.getCarId());
        if (null == car)
        {
            throw new BusinessException(String.format("无效的车辆Id: %d", monthlyTktParam.getCarId()));
        }
        
        //检查月票订单参数
        this.checkMonthlyTktDate(monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
        
        //检查是否有重复的月票订单
        if (this.existsValidMonthlyTkt(car, park, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate()))
        {
            throw new BusinessException("请勿重复购买月票");
        }
        
        String code = util.makeCode(OrderType.monthlyTicket);
        BigDecimal amt = this.calMonthlyTktAmt(park, car, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
        
        //创建订单
        Order order = Order.builder()
                .code(code).car(car).park(park).amt(amt)
                .realUnpayedAmt(amt)
                .type(OrderType.monthlyTicket)
                .startDate(monthlyTktParam.getStartDate())
                .endDate(monthlyTktParam.getEndDate())
                .status(OrderStatus.needToPay)
                .owner(owner).build();
        
        //保存订单
        this.save(order);
        
        OrderVo orderVo = OrderVo.builder().code(order.getCode()).orderId(order.getOrderId()).build();
        
        //开始付款
        return orderVo;
    }
    
    /**
     * 创建钱包充值订单
     * @param walletChargeParam
     * @throws Exception 
     */
    public WxUnifiedOrderResult createWalletChargeOrder(ChargeWalletParam walletChargeParam, String userName) throws Exception
    {
        User owner = userService.findByName(userName);
        
        String code = util.makeCode(OrderType.walletIn);
        
        //创建订单
        Order order = Order.builder()
                .code(code)
                .amt(walletChargeParam.getAmt())
                .realUnpayedAmt(walletChargeParam.getAmt())
                .type(OrderType.walletIn)
                .status(OrderStatus.needToPay)
                .owner(owner).build();
        
        //保存订单
        this.save(order);
        
        //开始付款
        return wxCmpt.unifiedOrder(order);
    }
    
    /**
     * 激活优惠券订单
     * @param activeCouponParam
     * @return
     * @throws Exception 
     */
    public WxUnifiedOrderResult createActiveCouponOrder(ActiveCouponParam activeCouponParam, String userName) throws Exception 
    {
        User owner = userService.findByName(userName);
        Coupon coupon = couponService.findOneById(activeCouponParam.getCouponId());
        if (null == coupon)
        {
            throw new BusinessException(String.format("需要激活的优惠券不存在, Id: %d", 
                    activeCouponParam.getCouponId()));
        }
        
        //检查优惠券状态: 只能激活过期的优惠券
        if (!coupon.getStatus().equals(CouponStatus.expired))
        {
            throw new BusinessException(String.format("优惠券在当前状态不能激活: %s", coupon.getStatus().getText()));
        }
        
        //检查金额是否一致
        if (!activeCouponParam.getAmt().equals(coupon.getActivePrice()))
        {
            throw new BusinessException("金额不正确");
        }
        
        //检查优惠券金额
        
        String code = util.makeCode(OrderType.coupon);
        
        //创建订单
        Order order = Order.builder()
                .code(code)
                .amt(activeCouponParam.getAmt())
                .realUnpayedAmt(activeCouponParam.getAmt())
                .activatedCoupon(coupon)
                .type(OrderType.coupon)
                .status(OrderStatus.needToPay)
                .owner(owner).build();
        
        //保存订单
        this.save(order);
        
        //开始付款
        return wxCmpt.unifiedOrder(order);
    }
    
    /**
     * 检查优惠券是否可用于订单
     * @param order 订单
     * @param coupon 被检查的优惠券
     * @param payerName 付款人
     * @return
     * @throws BusinessException 
     */
    private void checkCoupon(Order order, Coupon coupon, User payer) throws BusinessException
    {
        //检查coupon状态
        if (!coupon.getStatus().equals(CouponStatus.valid))
        {
            throw new BusinessException(String.format("优惠券处于: %s 状态, 不能使用", coupon.getStatus().getText()));
        }
        
        //检查有效期
        Date now = new DateTime().withTimeAtStartOfDay().toDate();
        if (now.before(coupon.getStartDate()))
        {
            throw new BusinessException("优惠券有效期还没开始");
        }
        if (now.after(coupon.getEndDate()))
        {
            throw new BusinessException("优惠券已经超过有效期");
        }
        
        //检查试用停车场
        if (0 < coupon.getApplicableParks().size()
                &&!coupon.getApplicableParks().contains(order.getPark()))
        {
            throw new BusinessException("优惠券不适用于当前停车场");
        }
        
        //检查优惠券拥有者
        if (!coupon.getOwner().getUserId().equals(payer.getUserId()))
        {
            throw new BusinessException("优惠券在他人名下, 不能使用");
        }
        
        //检查订单是否已经用过优惠券
        for (OrderPayment orderPayment : order.getOrderPayments())
        {
            if (null != orderPayment.getUsedCoupon())
            {
                throw new BusinessException("一张订单只能用一次优惠券");
            }
        }
    }
    
    /**
     * 计算使用优惠券后的金额
     * @param order
     * @param coupon
     * @return
     */
    private BigDecimal calRealAmt(Order order, Coupon coupon)
    {
        BigDecimal discount = order.getAmt().multiply(new BigDecimal(10).subtract(coupon.getDiscount()).divide(new BigDecimal(10), 2, RoundingMode.HALF_UP));
        BigDecimal realDiscount = coupon.getMaxAmt().min(discount);
        return order.getAmt().subtract(realDiscount).setScale(2);
    }
    
    /**
     * 检查订单是否可以支付
     * @param order
     * @return
     */
    private boolean canBePay(Order order, Date now) 
    {
        //无需付款
        if (order.getStatus().equals(OrderStatus.noNeedToPay))
        {
            return false;
        }
        
        //车辆已经出场的订单不能付款
        if (null != order.getIsOut() && order.getIsOut())
        {
            return false;
        }
        
        //从未付过款
        if (null == order.getOutTimeLimit())  
        {
            return true;
        }
        
        //已经付过款，但是超过了出场时间限制(提前付款超时未出场)
        if (now.after(order.getOutTimeLimit()))
        {
            return true;
        }
        
        return false;
    } 
    
    /**
     * 使用微信支付
     * @param order
     * @throws Exception 
     */
    public WxUnifiedOrderResult payByWx(PayOrderParam payParam, String payerName) throws Exception
    {
        //查找订单
        Order order = this.findOneByOrderId(payParam.getOrderId());
        if (null == order)
        {
            throw new BusinessException(String.format("无效的订单Id: %d", payParam.getOrderId()));
        }
        //检查订单状态
        if (!canBePay(order, new Date()))
        {
            throw new BusinessException(String.format("订单: %s 无需支付", order.getCode()));
        }
        
        //订单拥有者是付款人
        User payer = userService.findByName(payerName);
        if (null == payer)
        {
            throw new BusinessException(String.format("无效的用户: %d", payerName));
        }
        order.setOwner(payer);
        
        return wxCmpt.unifiedOrder(order);
    }
    
    /**
     * 使用钱包余额付款
     * @param order
     * @throws Exception 
     */
    public void payByWallet(PayOrderParam payParam, String payerName) throws Exception
    {
        //查找订单
        Order order = this.findOneByOrderId(payParam.getOrderId());
        if (null == order)
        {
            throw new BusinessException(String.format("无效的订单Id: %d", payParam.getOrderId()));
        }
        
        //订单拥有者是付款人
        User payer = userService.findByName(payerName);
        if (null == payer)
        {
            throw new BusinessException(String.format("无效的用户: %d", payerName));
        }
        order.setOwner(payer);
        
        //检查订单状态
        if (!canBePay(order, new Date()))
        {
            throw new BusinessException(String.format("订单: %s 无需支付", order.getCode()));
        }
        
        //只有停车订单才能使用优惠券
        Coupon coupon = null;
        if (order.getType().equals(OrderType.parking))
        {
            if (null != payParam.getCouponId()) //明确指定优惠券
            {
                //查找优惠券
                coupon = couponService.findOneById(payParam.getCouponId());
                if (null == coupon)
                {
                    throw new BusinessException(String.format("无效的优惠券Id: %d", payParam.getCouponId()));
                }
                
                //检查优惠券是否可用
                checkCoupon(order, coupon, payer);
            }
        }
        payByWallet(order, coupon);
    }
    
    /**
     * 使用钱包余额付款
     * @param order
     * @throws Exception 
     */
    public void quickPayByWallet(Order order) throws Exception
    {
        //检查订单状态
        if (!canBePay(order, new Date()))
        {
            throw new BusinessException(String.format("订单: %s 无需支付", order.getCode()));
        }
        
        //找到最合适的优惠券
        Coupon coupon = null;
        if (order.getType().equals(OrderType.parking))
        {
            coupon = couponService.findBest4Order(order);
        }
        
        //使用钱包付款
        this.payByWallet(order, coupon);
    }
    
    /**
     * 使用钱包余额付款
     * @param order
     */
    public void payByWallet(Order order, Coupon coupon) throws BusinessException
    {
        //检查拥有者
        User owner = order.getOwner();
        if (null == owner)
        {
            throw new BusinessException(String.format("订单: %s 是无主, 不能使用钱包支付", order.getCode()));
        }
        
        //余额不足
        if (0 > owner.getBalance().compareTo(order.getRealUnpayedAmt()))
        {
            throw new BusinessException(String.format("钱包余额: %.2f 元不足, 应付金额: %.2f 元",
                    owner.getBalance().floatValue(), order.getRealUnpayedAmt().floatValue()));
        }
        
        //设置coupon
        if (null != coupon)
        {
            //消耗优惠券
            couponService.useCoupon(coupon, order.getCode());
            //设置订单实付款金额
            order.setRealUnpayedAmt(calRealAmt(order, coupon));
            order.appedChangeRemark(String.format("使用优惠券: %s; ", coupon.getCode()));
        }

        //扣减钱包余额
        BigDecimal newBlance = owner.getBalance().subtract(order.getRealUnpayedAmt()).setScale(2, BigDecimal.ROUND_HALF_UP);
        order.appedChangeRemark(String.format("余额: %.2f 元 --> %.2f 元",  owner.getBalance().floatValue(), newBlance.floatValue()));
        owner.setBalance(newBlance);
        
        //设置订单支付
        order.setStatus(OrderStatus.payed);
        order.setPayedAmt(order.getAmt());
        BigDecimal realPayedAmt = order.getRealPayedAmt();
        if (null == realPayedAmt)
        {
            realPayedAmt = BigDecimal.ZERO;
        }
        order.setRealPayedAmt(realPayedAmt.add(order.getRealUnpayedAmt()));
        order.setRealUnpayedAmt(BigDecimal.ZERO);
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setUsedCoupon(coupon);
        orderPayment.setAmt(order.getAmt());
        orderPayment.setRealAmt(order.getRealUnpayedAmt());
        orderPayment.setOrder(order);
        orderPayment.setPaymentType(PaymentType.qb);
        orderPayment.setPaymentTime(new Date());
        orderPayment.setWalletBalance(newBlance);
        order.setLastPaymentTime(orderPayment.getPaymentTime());
        order.getOrderPayments().add(orderPayment);
        
        //记录备注
        this.save(order);
    }
    
    /**
     * 获取订单的出入库截图
     * @param order 指定的订单
     * @param eventType 出/入
     * @return
     * @throws IOException
     */
    public byte[] getOrderImage(Order order, DeviceUseage deviceUseage) throws IOException
    {
        String code = "";
        switch (deviceUseage)
        {
            case in:
                code = order.getInImgCode();
                break;
            case out:
                code = order.getOutImgCode();
                break;
            default:
                break;
        }
        return aliYunOssCmpt.getBytes(code);
    }
    
    /**
     * 查询用户可用优惠券数量
     * @param owner
     * @return
     */
    public Integer countValidMonthlyTktByOwner(User owner) 
    {
        Date today = new DateTime().withTimeAtStartOfDay().toDate();
        return orderDao.countByOwnerAndTypeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                owner, OrderType.monthlyTicket, OrderStatus.payed, today, today);
    }
    
    /**
     * 设置车辆是否抬杆出场
     * @param order
     * @throws BusinessException 
     */
    public void setOut(Order order, boolean isOut) throws BusinessException
    {
        order.setIsOut(isOut);
        this.save(order);
    }
    
    /**
     * 超时后，重新计算金额和出场时间
     * @param order
     * @throws BusinessException 
     * @throws ParseException 
     */
    public void resetAmtAndOutTimeLimit(Order order) throws ParseException, BusinessException
    {
        setAmtAndOutTimeLimit(order);
        if (0 > order.getPayedAmt().compareTo(order.getAmt())) //产生新的费用
        {
            order.setStatus(OrderStatus.needToPay);
        }
        save(order);
    }
}
