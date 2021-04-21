package com.kl.parkLine.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
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
import com.kl.parkLine.entity.KeyMap;
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
import com.kl.parkLine.enums.MonthlyMode;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PaymentType;
import com.kl.parkLine.enums.PlaceType;
import com.kl.parkLine.enums.RetCode;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.ActiveCouponParam;
import com.kl.parkLine.json.Base64Img;
import com.kl.parkLine.json.CalOrderAmtParam;
import com.kl.parkLine.json.CalOrderAmtResult;
import com.kl.parkLine.json.CarParam;
import com.kl.parkLine.json.ChargeWalletParam;
import com.kl.parkLine.json.ContentLines;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.json.MonthlyTktParam;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.RefundParam;
import com.kl.parkLine.json.TimePoint;
import com.kl.parkLine.json.WxPayNotifyParam;
import com.kl.parkLine.json.WxUnifiedOrderResult;
import com.kl.parkLine.json.XjPayParam;
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
    private final Logger logger = LoggerFactory.getLogger(OrderService.class);
    
    private final Integer EXPIRED_DAYS = 7;
    
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
    
    @Autowired
    private KeyMapService keyMapService;
    
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
            if (null!=log.getEvent() && log.getEvent().getType().equals(EventType.complete))
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
    public OrderVo findParkingByCar(CarParam carParam, String userName) throws ParseException, BusinessException
    {
        Car car = null;
        //根据车辆Id获取车辆
        if (null != carParam.getCarId())
        {
            car = carService.findOneById(carParam.getCarId());
            if (null == car)
            {
                throw new BusinessException(String.format("无效的车辆Id: %d", carParam.getCarId()));
            }
            carParam.setCarNo(car.getCarNo());
            carParam.setPlateColor(car.getPlateColor());
        }
        //根据车牌号码和颜色获取车辆
        else 
        {
            if (null == carParam.getCarNo())
            {
                throw new BusinessException("车牌号为空");
            }
            
            if (null == carParam.getPlateColor())
            {
                throw new BusinessException("车牌号颜色为空");
            }
            car = carService.getCar(carParam.getCarNo(), carParam.getPlateColor());
        }
        //绑定车辆
        if (null == car.getUser())
        {
            carService.bind(userName, carParam);
        }
        
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
                    .outTime(order.getOutTime())
                    .type(order.getType())
                    .inImgCode(order.getInImgCode())
                    .outImgCode(order.getOutImgCode())
                    .payedAmt(order.getPayedAmt())
                    .realPayedAmt(order.getRealPayedAmt())
                    .lastPaymentTime(order.getLastPaymentTime())
                    .startDate(order.getStartDate())
                    .endDate(order.getEndDate())
                    .status(OrderStatus.needToPay)
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
                .totalTmpCnt(park.getTotalTmpCnt())
                .availableTmpCnt(park.getAvailableTmpCnt())
                .totalGroundMonthlyCnt(park.getTotalGroundMonthlyCnt())
                .availableGroundMonthlyCnt(park.getAvailableGroundMonthlyCnt())
                .totalUndergroundMonthlyCnt(park.getTotalUndergroundMonthlyCnt())
                .availableUndergroundMonthlyCnt(park.getAvailableUndergroundMonthlyCnt())
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
                eventResult = EventResult.open(ContentLines.builder()
                        .line1(Const.TIME_STAMP)
                        .line2("一路平安")
                        .line3(order.getCar().getCarNo())
                        .line4(" ")
                        .voice(String.format("一路平安,%s", order.getCar().getCarNo()))
                        .build());
            }
            else if (order.getStatus().equals(OrderStatus.payed)) //已经支付
            {
                //未超过出场时限
                if (!now.after(order.getOutTimeLimit()))
                {
                    eventResult = EventResult.open(ContentLines.builder()
                            .line1(Const.TIME_STAMP)
                            .line2("一路平安")
                            .line3(order.getCar().getCarNo())
                            .line4(" ")
                            .voice(String.format("一路平安,%s", order.getCar().getCarNo()))
                            .build());
                }
            }
        }
        
        //如果开闸
        if (eventResult.getOpen())
        {
            //停车场空位+1
            Park park = order.getPark();
            Integer newAvailableCnt = park.getAvailableTmpCnt() + 1;
            park.setChangeRemark(String.format("停车完成, 临停可用车位变化: %d --> %d", 
                    park.getAvailableTmpCnt(), newAvailableCnt));
            park.setAvailableTmpCnt(newAvailableCnt);
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
        
        //检查车辆是否重复入场
        /*if (existsByTypeAndCarAndStatus(OrderType.parking, car, OrderStatus.in))
        {
            return EventResult.notOpen(ContentLines.builder()
                    .line1(Const.TIME_STAMP)
                    .line2(event.getPlateNo())
                    .line3("车已在场")
                    .line4(" ")
                    .voice("车已在场").build());
        }*/
        
        //检查是否为月票车
        Order monthlyTck = this.findValidMonthlyTck(car, park);
        if (null == monthlyTck) //无月票
        {
            //停车场无空位
            if (0 >= park.getAvailableTmpCnt())
            {
                
                return EventResult.notOpen(ContentLines.builder()
                        .line1(Const.TIME_STAMP)
                        .line2(event.getPlateNo())
                        .line3("车位已满")
                        .line4(" ")
                        .voice("车位已满").build());
            }
            
            //车辆不在白名单中才检查黑名单情况,也就是说，如果车辆在白名单中，则直接放行
            if (!parkCarItemService.existsInWhiteList(park, car))
            {
                //检查是否在黑名单
                if (parkCarItemService.existsInBlackList(park, car))
                {
                    return EventResult.notOpen(
                            ContentLines.builder()
                            .line1(Const.TIME_STAMP)
                            .line2("禁止入场")
                            .line3(event.getPlateNo())
                            .line4(" ")
                            .voice(String.format("禁止入场,%s", event.getPlateNo())).build());
                }
            }
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
        EventResult eventResult = null;
        if (null != monthlyTck) //月租车进入
        {
            DateTime now = new DateTime(event.getTimeIn());
            DateTime endDate = new DateTime(monthlyTck.getEndDate());
            order.setUsedMonthlyTkt(monthlyTck);
            //月租车提示
            int nDay = Days.daysBetween(now, endDate).getDays();
            eventResult = EventResult.open(ContentLines.builder()
                    .line1(Const.TIME_STAMP)
                    .line2("欢迎光临")
                    .line3(event.getPlateNo())
                    .line4(String.format("月租车剩余%d天", nDay))
                    .voice(String.format("欢迎光临,月租车剩余%d天", nDay))
                    .build());
        }
        else //临停车辆进入, 扣减停车位
        {
            //停车场空位-1
            Integer newAvailableCnt = park.getAvailableTmpCnt() - 1;
            park.setChangeRemark(String.format("车辆入场, 临停可用车位变化: %d --> %d, 事件: %s", 
                    park.getAvailableTmpCnt(), newAvailableCnt, event.getGuid()));
            park.setAvailableTmpCnt(newAvailableCnt);
            parkService.save(park);
            
            //临停车提示
            eventResult = EventResult.open(ContentLines.builder()
                    .line1(Const.TIME_STAMP)
                    .line2("欢迎光临")
                    .line3(event.getPlateNo())
                    .line4("临时车")
                    .voice(String.format("欢迎光临,%s", event.getPlateNo()))
                    .build());
        }
        
        //保存 订单
        this.save(order, event);
        return eventResult;
    }
    
    /**
     * 检查是否存在订单
     * @return
     */
    public Boolean existsByTypeAndCarAndStatus(OrderType orderType, Car car, OrderStatus orderStatus)
    {
        return orderDao.existsByTypeAndCarAndStatus(OrderType.parking, car, orderStatus);
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
            Car car = carService.getCar(event.getPlateNo(), event.getPlateColor());
            Device device = deviceService.findOneBySerialNo(event.getDeviceSn());
            order = orderDao.findTopByCarAndParkAndTypeAndIsOutIsFalseOrderByInTimeDesc(car, device.getPark(), OrderType.parking);
        }
        
        if (null == order) //无入场记录,不开闸
        {
            return EventResult.notOpen(ContentLines.builder()
                    .line1(Const.TIME_STAMP)
                    .line2(event.getPlateNo())
                    .line3("无入场")
                    .line4(" ")
                    .build());
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
            if (0 == order.getAmt().compareTo(BigDecimal.ZERO))  //无需付款,直接开闸
            {
                order.setStatus(OrderStatus.noNeedToPay);
                //月票车出场
                Order monthlyTkt = order.getUsedMonthlyTkt();
                if (null != monthlyTkt)
                {
                    int nDay = Days.daysBetween(new DateTime(now), new DateTime(monthlyTkt.getEndDate())).getDays();
                    eventResult = EventResult.open(ContentLines.builder()
                            .line1(Const.TIME_STAMP)
                            .line2("一路平安")
                            .line3(event.getPlateNo())
                            .line4(String.format("月租车剩余%d天", nDay))
                            .voice(String.format("一路平安,月租车剩余%d天", nDay))
                            .build());
                }
                else
                {
                    eventResult = EventResult.open(ContentLines.builder()
                            .line1(Const.TIME_STAMP)
                            .line2("一路平安")
                            .line3(event.getPlateNo())
                            .line4(" ")
                            .voice(String.format("一路平安,%s", event.getPlateNo()))
                            .build());
                }
                
            }
            else  //产生费用
            {
                order.setStatus(OrderStatus.needToPay);
                DateTime inTime = new DateTime(order.getInTime());
                DateTime outTime = new DateTime(order.getOutTime());
                Period period = new Period(inTime, outTime, PeriodType.time());
                String line3 = String.format("停车%d小时%d分", 
                        period.getHours(), period.getMinutes());
                String line4 = String.format("%.2f元", 
                        order.getRealUnpayedAmt().floatValue());
                //用户开通了无感支付
                if (null!=order.getOwner() && order.getOwner().getIsQuickPay())
                {
                    try
                    {
                        this.quickPayByWallet(order); //无感支付, 钱包支付订单
                        /*eventResult = EventResult.open(ContentLines.builder()
                                .line1(Const.TIME_STAMP)
                                .line2(event.getPlateNo())
                                .line3(String.format("停车时长%d小时%d分", 
                                        period.getHours(), period.getMinutes()))
                                .line4(String.format("无感支付%.2f元",order.getAmt().floatValue()))
                                .voice(String.format("一路平安,%s", event.getPlateNo()))
                                .build());*/
                        eventResult = EventResult.open(ContentLines.builder()
                                .line1(Const.TIME_STAMP)
                                .line2("一路平安")
                                .line3(event.getPlateNo())
                                .line4(" ")
                                .voice(String.format("一路平安,%s", event.getPlateNo()))
                                .build());
                    }
                    catch (Exception e)  //无感支付失败
                    {
                        OrderLog log = OrderLog.builder().order(order).build();
                        log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                        order.getLogs().add(log);
                        order.setStatus(OrderStatus.needToPay);
                        eventResult = EventResult.notOpen(ContentLines.builder()
                                .line1(Const.TIME_STAMP)
                                .line2(event.getPlateNo())
                                .line3(line3)
                                .line4(line4)
                                .dr((byte) 0)
                                .voice(String.format("%s,请交费%s", event.getPlateNo(),line4))
                                .build());
                    }
                }
                else //用户未开通无感支付
                {
                    order.setStatus(OrderStatus.needToPay);
                    eventResult = EventResult.notOpen(ContentLines.builder()
                            .line1(Const.TIME_STAMP)
                            .line2(event.getPlateNo())
                            .line3(line3)
                            .line4(line4)
                            .voice(String.format("%s,请交费%s", event.getPlateNo(),line4))
                            .dr((byte) 0)
                            .build());
                }
            }
        }
        else //订单已经支付
        {
            //检查是否已经超过出场限制
            if (!order.getOutTime().after(order.getOutTimeLimit())) //未超过时间限制
            {
                eventResult = EventResult.open(ContentLines.builder()
                        .line1(Const.TIME_STAMP)
                        .line2("一路平安")
                        .line3(event.getPlateNo())
                        .line4(" ")
                        .voice(String.format("一路平安,%s", event.getPlateNo()))
                        .build());
            }
            else //超过离场时间限制
            {
                DateTime outTimeLimit = new DateTime(order.getOutTimeLimit());
                DateTime endTime = new DateTime(now);
                Period period = new Period(endTime, outTimeLimit, PeriodType.time());
                //计算需要补缴的费用
                resetAmtAndOutTimeLimit(order);
                //提示
                String line3 = String.format("停车%d小时%d分", 
                        period.getHours(), period.getMinutes());
                String line4 = String.format("%.2f元", 
                        order.getRealUnpayedAmt().floatValue());
                if (order.getStatus().equals(OrderStatus.needToPay))  //产生新的费用
                {
                    //BigDecimal unPayedAmt = order.getAmt().subtract(order.getPayedAmt());
                    //用户开通了无感支付
                    if (order.getOwner().getIsQuickPay())
                    {
                        try
                        {
                            this.quickPayByWallet(order); //无感支付, 钱包支付订单
                            eventResult = EventResult.open(ContentLines.builder()
                                    .line1(Const.TIME_STAMP)
                                    .line2("一路平安")
                                    .line3(event.getPlateNo())
                                    .line4(" ")
                                    .voice(String.format("一路平安,%s", event.getPlateNo()))
                                    .build());
                            /*eventResult = EventResult.open(String.format("超时%d小时%d分, 无感支付%.2f元", 
                                    period.getHours(), period.getMinutes(), unPayedAmt.floatValue()));*/
                        }
                        catch (Exception e)
                        {
                            OrderLog log = OrderLog.builder().order(order).build();
                            log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                            order.getLogs().add(log);
                            order.setStatus(OrderStatus.needToPay);
                            eventResult = EventResult.notOpen(ContentLines.builder()
                                    .line1(Const.TIME_STAMP)
                                    .line2(event.getPlateNo())
                                    .line3(line3)
                                    .line4(line4)
                                    .voice(String.format("%s,请缴费%s", event.getPlateNo(), line4))
                                    .dr((byte) 0)
                                    .build());
                        }
                    }
                    else //用户未开通无感支付
                    {
                        order.setStatus(OrderStatus.needToPay);
                        eventResult = EventResult.notOpen(ContentLines.builder()
                                .line1(Const.TIME_STAMP)
                                .line2(event.getPlateNo())
                                .line3(String.format("超时%d小时%d分", 
                                        period.getHours(), period.getMinutes()))
                                .line4(line4)
                                .dr((byte) 0)
                                .voice(String.format("%s,请交费%s", event.getPlateNo(),line4))
                                .build());
                    }
                }
                else  //超时未产生新的费用
                {
                    eventResult = EventResult.open(ContentLines.builder()
                            .line1(Const.TIME_STAMP)
                            .line2("一路平安")
                            .line3(event.getPlateNo())
                            .line4(" ")
                            .voice(String.format("一路平安,%s", event.getPlateNo()))
                            .build());
                }
            }
        }
        
        //如果开闸并且不是月租车出场
        if (eventResult.getOpen()&&null!=order.getUsedMonthlyTkt())
        {
            //停车场空位+1
            Park park = order.getPark();
            Integer newAvailableCnt = park.getAvailableTmpCnt() + 1;
            park.setChangeRemark(String.format("停车完成, 临停可用车位变化: %d --> %d, 事件: %s", 
                    park.getAvailableTmpCnt(), newAvailableCnt, event.getGuid()));
            park.setAvailableTmpCnt(newAvailableCnt);
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
            return EventResult.notOpen(ContentLines.builder()
                    .line1(String.format("无效事件Id: %s", event.getActId())).build());
        }
        
        //取消targetEvent
        Park park = order.getPark();
        Event targetEvent = eventService.findOneByGuidAndParkCode(event.getTargetGuid(), park.getCode());
        if (null == targetEvent) //未找到被取消的事件
        {
            return EventResult.notOpen(ContentLines.builder()
                    .line1(String.format("无效事件Id: %s", event.getActId())).build());
        }
        
        //如果订单已经付款以及后续状态，返回失败
        if (OrderStatus.payed.getValue() <= order.getStatus().getValue())
        {
            String msg = String.format("停车订单【%s】处于【%s】状态, 无法撤销", 
                    order.getCode(), order.getStatus().getText());
            event.setRemark(msg);
            return EventResult.notOpen(ContentLines.builder()
                    .line1(msg).build());
        }
        
        // 取消的是入场事件，取消订单
        if (EventType.in.getValue() == event.getTargetType().getValue())
        {
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //当前订单状态是入场，停车场空位数量+1
            if (OrderStatus.in.getValue() == order.getStatus().getValue())
            {
                Integer newAvailableCnt = park.getAvailableTmpCnt() + 1;
                park.setChangeRemark(String.format("取消车辆入场, 临停可用车位变化: %d --> %d, 事件: %s, 被取消事件: %s", 
                        park.getAvailableTmpCnt(), newAvailableCnt, 
                        event.getGuid(), targetEvent.getGuid()));
                park.setAvailableTmpCnt(newAvailableCnt);
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
            Integer newAvailableCnt = park.getAvailableTmpCnt() - 1;
            park.setChangeRemark(String.format("取消车停车完成, 临停可用车位变化: %d --> %d, 事件: %s, 被取消事件: %s", 
                    park.getAvailableTmpCnt(), newAvailableCnt, 
                    event.getGuid(), targetEvent.getGuid()));
            park.setAvailableTmpCnt(newAvailableCnt);
            parkService.save(park);
            park.setAvailableTmpCnt(newAvailableCnt);
        }
        
        //保存订单
        this.save(order, event);
        
        //禁用目标事件
        targetEvent.setEnabled("N");
        eventService.save(targetEvent);
        return EventResult.open(ContentLines.builder().line1("处理成功").build());
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
        if (0 == minutes)
        {
            order.setAmt(BigDecimal.ZERO);
            order.setRealUnpayedAmt(BigDecimal.ZERO);
            return;
        }
        
        //阶梯计费
        if (park.getChargeType().equals(ChargeType.step))
        {
            //找到最大的计费周期
            ParkStepFee parkStepFee = parkStepFeeDao.findTopByParkAndCarTypeOrderByEndMinDesc(park, car.getCarType());
            amt = new BigDecimal(minutes).divide(new BigDecimal(parkStepFee.getEndMin()),RoundingMode.DOWN).multiply(parkStepFee.getAmt());
            minutes = minutes % parkStepFee.getEndMin();
            parkStepFee = parkStepFeeDao.findOneByParkAndCarTypeAndStartMinLessThanEqualAndEndMinGreaterThanEqual(
                    park, car.getCarType(), minutes, minutes);
            if (null == parkStepFee)
            {
                throw new BusinessException(String.format("停车场  %s 阶梯费用配置错误, 缺少: %s 车  %d 分钟收费配置", 
                        park.getName(), car.getCarType().getText(), minutes));
            }
            amt = amt.add(parkStepFee.getAmt());
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
            //realUnpayedAmt = amt-payedAmt
            order.setRealUnpayedAmt(amt.subtract(order.getPayedAmt()));
        }
        else
        {
            order.setRealUnpayedAmt(amt);
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
                        qOrder.endDate.as("orderEndDate")))
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
        Order order = orderDao.findOneByPayCode(wxPayNotifyParam.getOutTradeNo());
        if (null == order)
        {
            logger.info(String.format("无效的订单号:%s", wxPayNotifyParam.getOutTradeNo()));
            return;
        }
        if (!canBePay(order))  //已经处理过付款通知(微信会重复推送同一张订单的付款通知)
        {
            logger.info(String.format("订单: %s 无需支付", order.getCode()));
            return;
        }
        //修改订单状态
        order.setStatus(OrderStatus.payed);
        //微信订单号
        order.setWxTransactionId(wxPayNotifyParam.getTransactionId());
        //设置用户关注公众号情况
        order.getOwner().setSubscribe(wxPayNotifyParam.getIsSubscribe());
        
        //设置订单支付
        paySucess(order, PaymentType.wx, wxPayNotifyParam.getBankType(), null, wxPayNotifyParam.getTimeEnd());
        
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
     * 是否使用老月票价格进行续费
     * @param oldTkt 老的月票订单
     * @param placeType 新的车位类型
     * @return
     */
    private boolean useOldTktPrice(Order oldTkt, PlaceType placeType)
    {
        //老订单有值
        if (null == oldTkt)
        {
            return false;
        }
        
        //停车场设置,非灵活月票价格
        if (!oldTkt.getPark().getMonthlyMode().equals(MonthlyMode.noFix))
        {
            return false;
        }
        
        //车位类型新老一致
        if (!oldTkt.getPlaceTye().equals(placeType))
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * 计算月票价格
     * @param oldTkt 被续费的订单
     * @param park 停车场
     * @param car 车辆
     * @param dateTimeStart 开始时间
     * @param dateTimeEnd 结束时间
     * @return
     * @throws BusinessException 
     */
    private BigDecimal calMonthlyTktAmt(Order oldTkt, Park park, Car car, PlaceType placeType, Date startDate, Date endDate) throws BusinessException
    {
        BigDecimal amt = BigDecimal.ZERO;
        BigDecimal price = BigDecimal.ZERO;
        if (useOldTktPrice(oldTkt, placeType)) // 老月票续费并且车位类型相同,根据老订单取价格
        {
            price = oldTkt.getMonthlyPrice();
        }
        else
        {
            if (placeType.equals(PlaceType.ground))
            {
                if (!park.getHasGroundPlace())
                {
                    throw new BusinessException(String.format("该停车场不支持 %s 车位", placeType.getText()));
                }
                price = park.getFuelGroundMonthlyPrice(); //默认按照燃油车计费
                if (car.getCarType().equals(CarType.newEnergy))
                {
                    price = park.getNewEnergyGroundMonthlyPrice();
                }
            }
            else
            {
                if (!park.getHasUndergroundPlace())
                {
                    throw new BusinessException(String.format("该停车场不支持 %s 车位", placeType.getText()));
                }
                price = park.getFuelUndergroundMonthlyPrice(); //默认按照燃油车计费
                if (car.getCarType().equals(CarType.newEnergy))
                {
                    price = park.getNewEnergyUndergroundMonthlyPrice();
                }
            }
            park.getFuelGroundMonthlyPrice(); //默认按照燃油车计费
            if (car.getCarType().equals(CarType.newEnergy))
            {
                price = park.getNewEnergyGroundMonthlyPrice();
            }
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
    public BigDecimal inqueryMonthlyTkt(MonthlyTktParam monthlyTktParam) throws BusinessException
    {
        Order oldTkt = null;
        Park park = null;
        Car car = null;
        if (null != monthlyTktParam.getOrderId())
        {
            oldTkt = findOneByOrderId(monthlyTktParam.getOrderId());
            park = oldTkt.getPark();
            car = oldTkt.getCar();
        }
        else
        {
            park = parkService.findOneById(monthlyTktParam.getParkId());
            car = carService.findOneById(monthlyTktParam.getCarId());
        }
        
        checkMonthlyTktDate(monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
        
        //检查是否有重复的月票订单
        if (this.existsValidMonthlyTkt(car, park, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate()))
        {
            throw new BusinessException("请勿重复购买月票");
        }
        
        return calMonthlyTktAmt(oldTkt, park, car, monthlyTktParam.getPlaceType(), monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
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
    public OrderVo createMonthlyTkt(MonthlyTktParam monthlyTktParam, String userName) throws BusinessException
    {
        Order oldTkt = null;
        User owner = null;
        Park park = null;
        Car car = null;
        if (null != monthlyTktParam.getOrderId())
        {
            oldTkt = findOneByOrderId(monthlyTktParam.getOrderId());
            owner = oldTkt.getOwner();
            park = oldTkt.getPark();
            car = oldTkt.getCar();
        }
        else
        {
            owner = userService.findByName(userName);
            park = parkService.findOneById(monthlyTktParam.getParkId());
            car = carService.findOneById(monthlyTktParam.getCarId());
        }
        
        //检查月票订单参数
        //this.checkMonthlyTktDate(monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
        
        //检查是否有重复的月票订单
        if (this.existsValidMonthlyTkt(car, park, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate()))
        {
            throw new BusinessException("请勿重复购买月票");
        }
        
        String code = util.makeCode(OrderType.monthlyTicket);
        BigDecimal amt = this.calMonthlyTktAmt(oldTkt, park, car, monthlyTktParam.getPlaceType(), monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate());
        
        //创建订单
        Order order = Order.builder()
                .code(code).car(car).park(park).amt(amt)
                .realUnpayedAmt(amt)
                .type(OrderType.monthlyTicket)
                .placeTye(monthlyTktParam.getPlaceType())
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
                .payCode(code)
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
                .payCode(code)
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
        if (null != order.getOrderPayments())
        {
            for (OrderPayment orderPayment : order.getOrderPayments())
            {
                if (null != orderPayment.getUsedCoupon())
                {
                    throw new BusinessException("一张订单只能用一次优惠券");
                }
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
        BigDecimal realDiscount = coupon.getMaxAmt().min(discount).setScale(2, RoundingMode.HALF_UP);
        return order.getAmt().subtract(realDiscount).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 检查订单是否可以支付
     * @param order
     * @return
     */
    private boolean canBePay(Order order) 
    {
        //需要付款
        if (order.getStatus().equals(OrderStatus.needToPay))  
        {
            return true;
        }
        
        //车辆在场,从未付过款,可以提前支付
        if (order.getStatus().equals(OrderStatus.in))
        {
            return true;
        }
        
        //已经付过款，但是超过了出场时间限制(提前付款超时未出场)
        Date now = new Date();
        if (order.getStatus().equals(OrderStatus.payed) && now.after(order.getOutTimeLimit()))
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
        if (!canBePay(order))
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
        
        //PayCode 为了防止提前支付停车订单超时后，由于单号重复无法微信支付的问题
        if (0 == order.getOrderPayments().size())
        {
            order.setPayCode(order.getCode());
        }
        else
        {
            order.setPayCode(String.format("%s-%d", order.getCode(), order.getOrderPayments().size()));
        }
        this.save(order);
        
        return wxCmpt.unifiedOrder(order);
    }
    
    /**
     * 使用钱包余额付款
     * @param order
     * @throws Exception 
     */
    public void payByWallet(PayOrderParam payParam, String payerName) throws BusinessException
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
        if (!canBePay(order))
        {
            throw new BusinessException(String.format("订单: %s 无需支付", order.getCode()));
        }
        
        //只有停车订单才能使用优惠券
        Coupon coupon = null;
        if (order.getType().equals(OrderType.parking) && null!=payParam.getCouponId())
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
        if (!canBePay(order))
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
        
        //设置coupon
        if (null != coupon)
        {
            //设置订单实付款金额
            order.setRealUnpayedAmt(calRealAmt(order, coupon));
            //消耗优惠券
            couponService.useCoupon(coupon, order);
            order.appedChangeRemark(String.format("使用优惠券: %s; ", coupon.getCode()));
        }
        
        //余额不足
        if (0 > owner.getBalance().compareTo(order.getRealUnpayedAmt()))
        {
            throw new BusinessException(RetCode.balanceNotEnough, String.format("钱包余额: %.2f 元不足, 应付金额: %.2f 元",
                    owner.getBalance().floatValue(), order.getRealUnpayedAmt().floatValue()));
        }

        //扣减钱包余额
        BigDecimal newBlance = owner.getBalance().subtract(order.getRealUnpayedAmt()).setScale(2, BigDecimal.ROUND_HALF_UP);
        order.appedChangeRemark(String.format("余额: %.2f 元 --> %.2f 元",  owner.getBalance().floatValue(), newBlance.floatValue()));
        owner.setBalance(newBlance);
        
        //设置订单支付
        paySucess(order, PaymentType.qb, null, coupon, new Date());
        
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
    public Base64Img getOrderImage(Order order, DeviceUseage deviceUseage) throws IOException
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
        Base64Img base64Img = new Base64Img();
        if (!StringUtils.isEmpty(code))
        {
            base64Img.setImg(aliYunOssCmpt.getBase64(code));
        }
        return base64Img;
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
    
    /**
     * 找到指定车辆在指定停车场的有效月票
     * @param car
     * @param park
     * @return
     */
    public Order findValidMonthlyTck(Car car, Park park)
    {
        Date now = new Date();
        return orderDao.findTopByTypeAndCarAndParkAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqualOrderByEndDateDesc(OrderType.monthlyTicket, car, park, OrderStatus.payed, now, now);
    }
    
    /**
     * 处理订单付款成功
     * @param order 付款成功的订单
     * @param paymentType 付款方式
     * @param bankType 付款银行（微信付款时才有此参数）
     * @param coupon 使用的优惠券(钱包付款才有此参数)
     * @param paymentTime
     * @throws BusinessException 
     */
    public void paySucess(Order order, PaymentType paymentType, String bankType, Coupon coupon, Date paymentTime) throws BusinessException
    {
        //设置订单支付
        order.setStatus(OrderStatus.payed);
        order.setPayedAmt(order.getAmt());
        BigDecimal realPayedAmt = order.getRealPayedAmt();
        if (null == realPayedAmt)
        {
            realPayedAmt = BigDecimal.ZERO;
        }
        order.setRealPayedAmt(realPayedAmt.add(order.getRealUnpayedAmt()));
        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setOrder(order);
        orderPayment.setAmt(order.getAmt());
        orderPayment.setRealAmt(order.getRealUnpayedAmt());
        orderPayment.setPaymentType(paymentType);
        orderPayment.setPaymentTime(paymentTime);
        
        //微信付款特有
        orderPayment.setBankType(bankType);
        
        //现金付款时，订单可能无拥有者
        if (null != order.getOwner()) 
        {
            orderPayment.setWalletBalance(order.getOwner().getBalance());
        }
        
        //钱包付款特有
        orderPayment.setUsedCoupon(coupon);
        
        order.setLastPaymentTime(paymentTime);
        order.setRealUnpayedAmt(BigDecimal.ZERO);
        if (null == order.getOrderPayments())
        {
            order.setOrderPayments(new ArrayList<OrderPayment>());
        }
        order.getOrderPayments().add(orderPayment);
        
        switch (order.getType())
        {
            case walletIn: //钱包充值订单:增加钱包余额
                User owner = order.getOwner();
                owner.setBalance(owner.getBalance().add(order.getAmt()));
                orderPayment.setWalletBalance(owner.getBalance());//记录钱包余额
                break;
            case coupon: //激活优惠券
                Coupon activedCoupon = order.getActivatedCoupon();
                activedCoupon.setStatus(CouponStatus.valid);
                //默认激活七天
                activedCoupon.setStartDate(paymentTime);
                activedCoupon.setEndDate(new DateTime(paymentTime).plusDays(Const.COUPON_ACTIVE_DAYS).toDate());
                break;
            case monthlyTicket: //月租成功
                //停车场可用月租数量-1
                parkService.changeMonthlyAvaliableCnt(order, -1);
                break;
            case refund: //退款
                owner = order.getOwner();
                owner.setBalance(owner.getBalance().subtract(order.getAmt()));
                orderPayment.setWalletBalance(owner.getBalance());//记录钱包余额
                break;
            default:
                break;
        }
    }
    
    /**
     * 定时任务执行，更新已经过期的月票并且释放可用月租车位数量
     * @throws BusinessException 
     */
    public void updateExpiredMonthlyTkt() throws BusinessException
    {
        Date now = new Date();
        List<Order> expiredOrders = orderDao.findByTypeAndStatusAndEndDateLessThan(OrderType.monthlyTicket, OrderStatus.payed, now);
        for (Order order : expiredOrders)
        {
            //增加停车场可用固定车位
            parkService.changeMonthlyAvaliableCnt(order, 1);
            //设置状态过期
            order.setStatus(OrderStatus.expired);
            order.appedChangeRemark(String.format("月票  %s 过期", order.getCode()));
            save(order);
        }
    }
    
    /**
     * 计算订单价格
     * @param calOrderAmtParam
     * @return
     * @throws BusinessException 
     * @throws ParseException 
     */
    public CalOrderAmtResult calOrderAmt(CalOrderAmtParam calOrderAmtParam) throws BusinessException, ParseException
    {
        Order order = this.findOneByOrderId(calOrderAmtParam.getOrderId());
        if (null == order)
        {
            throw new BusinessException(String.format("无效的订单Id: %d", calOrderAmtParam.getOrderId()));
        }
        //只计算停车订单的价格
        if (!order.getType().equals(OrderType.parking))
        {
            throw new BusinessException("非停车订单");
        }
        //入场时间为空
        if (null == order.getInTime())
        {
            throw new BusinessException("入场时间为空");
        }
        order.setOutTime(calOrderAmtParam.getOutTime());
        this.setAmtAndOutTimeLimit(order);
        this.save(order);
        return CalOrderAmtResult.builder().amt(order.getAmt()).build();
    }
    
    /**
     * 现金支付通知
     * @param wxPayNotifyParam
     * @throws UnsupportedEncodingException 
     */
    public void xjPaySuccess(XjPayParam xjPayParam) throws BusinessException, UnsupportedEncodingException
    {
        //获取私钥
        KeyMap keyMap = keyMapService.findOneByPublicKey(xjPayParam.getPublicKey());
        if (null == keyMap)
        {
            throw new BusinessException(String.format("无效的公钥: %s", xjPayParam.getPublicKey()));
        }
        
        //校验参数签名
        String parmas = String.format("orderId=%d&realPayedAmt=%.2f&paymentTime=%d&payee=%s&remark=%s&publicKey=%s&privateKey=%s", 
                xjPayParam.getOrderId(), 
                xjPayParam.getRealPayedAmt().setScale(2, RoundingMode.HALF_UP).floatValue(), 
                xjPayParam.getPaymentTime(), xjPayParam.getPayee(),
                xjPayParam.getRemark(), xjPayParam.getPublicKey(), keyMap.getPrivateKey());
        String md5 = DigestUtils.md5DigestAsHex(parmas.getBytes("UTF-8"));
        if (!md5.equals(xjPayParam.getSign()))
        {
            logger.error(String.format("%s,%s,%s", md5, xjPayParam.getSign(),parmas));
            throw new BusinessException("无效的签名");
        }
        
        Order order = findOneByOrderId(xjPayParam.getOrderId());
        if (null == order)
        {
            throw new BusinessException(String.format("无效的订单Id: %d", xjPayParam.getOrderId()));
        }
        
        //处理支付成功
        paySucess(order, PaymentType.xj, null, null, new Date(xjPayParam.getPaymentTime()));
        
        //现金支付以实际付款金额为准
        order.setRealPayedAmt(xjPayParam.getRealPayedAmt());
        
        //记录现金收款人
        order.setRemark(xjPayParam.getRemark());
        order.setCashPayee(xjPayParam.getPayee());
        order.appedChangeRemark(String.format("现金收款人: %s", xjPayParam.getPayee()));
        order.setIsOut(true);
        //记录备注
        this.save(order);
    }
    
    /**
     * 记录退款
     * @param wxPayNotifyParam
     * @throws UnsupportedEncodingException 
     */
    public void refundSuccess(RefundParam refundParam) throws BusinessException, UnsupportedEncodingException
    {
        //获取私钥
        KeyMap keyMap = keyMapService.findOneByPublicKey(refundParam.getPublicKey());
        if (null == keyMap)
        {
            throw new BusinessException(String.format("无效的公钥: %s", refundParam.getPublicKey()));
        }
        
        //校验参数签名
        String parmas = String.format("userId=%d&amt=%.2f&refundBy=%s&refundTime=%d&remark=%s&publicKey=%s&privateKey=%s", 
                refundParam.getUserId(),
                refundParam.getAmt().setScale(2, RoundingMode.HALF_UP).floatValue(), 
                refundParam.getRefundBy(), refundParam.getRefundTime(),
                refundParam.getRemark(), refundParam.getPublicKey(), keyMap.getPrivateKey());
        String md5 = DigestUtils.md5DigestAsHex(parmas.getBytes("UTF-8"));
        if (!md5.equals(refundParam.getSign()))
        {
            logger.error(String.format("%s,%s,%s", md5, refundParam.getSign(), parmas));
            throw new BusinessException("无效的签名");
        }
        
        //找到对应user
        User user = userService.findOneById(refundParam.getUserId());
        if (null == user)
        {
            throw new BusinessException(String.format("无效的用户Id: %d", refundParam.getUserId()));
        }
        
        //钱包余额不足
        if (0 > user.getBalance().compareTo(refundParam.getAmt()))
        {
            throw new BusinessException("用户余额不足");
        }
        
        //新增退款订单
        Order order = new Order();
        order.setCode(util.makeCode(OrderType.refund));
        order.setOwner(user);
        order.setType(OrderType.refund);
        order.setAmt(refundParam.getAmt());
        order.setRealUnpayedAmt(refundParam.getAmt());
        paySucess(order, PaymentType.wx, null, null, new Date(refundParam.getRefundTime()));
        
        //记录退款操作人
        order.setRemark(refundParam.getRemark());
        order.appedChangeRemark(String.format("退款操作人: %s", refundParam.getRefundBy()));
        
        //记录备注
        this.save(order);
    }
    
    /**
     * 找到即将过期的月票
     * @return
     */
    public List<Order> findExpiringMonthlyTkt()
    {
        DateTime now = new DateTime();
        DateTime endDate = now.withMillisOfDay(0).plusDays(EXPIRED_DAYS); //提前7天发过期提醒
        return orderDao.findExpiringMonthlyTkt(endDate.toDate());
    }
}
