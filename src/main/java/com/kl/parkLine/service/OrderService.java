package com.kl.parkLine.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkFixedFee;
import com.kl.parkLine.entity.ParkStepFee;
import com.kl.parkLine.entity.QCar;
import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.QPark;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CarType;
import com.kl.parkLine.enums.ChargeType;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.PaymentType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.ActiveCouponParam;
import com.kl.parkLine.json.ChargeWalletParam;
import com.kl.parkLine.json.MonthlyTktParam;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.WxPayNotifyParam;
import com.kl.parkLine.json.WxUnifiedOrderResult;
import com.kl.parkLine.predicate.OrderPredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.OrderVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
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
    
    @Autowired
    private OrderPredicates orderPredicates;
    
    @Autowired
    private IParkStepFeeDao parkStepFeeDao;
    
    @Autowired
    private Utils util;
    
    
    @Autowired
    private WxCmpt wxCmpt;
    
    @Autowired
    private CouponService couponService;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    @Autowired
    private DeviceService deviceService;
    
    private final Float ACTIVE_COUPON_FACTOR = 0.8f; //激活优惠券金额与券面金额系数
    private final List<OrderStatus> checkedStatus = new ArrayList<OrderStatus>();
    
    //已经完成的订单状态
    private final List<OrderStatus> completedStauts = new ArrayList<OrderStatus>();
    
    public OrderService()
    {
        //检查重复订单包含的状态: 存在等待付款和已经付款的月票订单时，不能再次创建重复的月票
        checkedStatus.add(OrderStatus.needToPay);
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
    private Page<OrderVo> fuzzyFindPage(Predicate searchPred, Pageable pageable)
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
                        qOrder.startDate,
                        qOrder.endDate))
                .from(qOrder).leftJoin(qPark).on(qOrder.park.eq(qPark))
                .leftJoin(qCar).on(qOrder.car.eq(qCar))
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
    
    /**
     * 作为终端用户分页查询
     * @param orderVo
     * @param pageable
     * @param userName
     * @return
     */
    public Page<OrderVo> fuzzyFindPageAsUser(OrderVo orderVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        
        Predicate searchPred = orderPredicates.fuzzyAsEndUser(orderVo, user);
        
        return fuzzyFindPage(searchPred, pageable);
    }
    
    /**
     * 作为后台管理(停车场/管理员)分页查询
     * @param orderVo
     * @param pageable
     * @param userName
     * @return
     */
    public Page<OrderVo> fuzzyFindPageAsManager(OrderVo orderVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        
        Predicate searchPred = orderPredicates.fuzzyAsManager(orderVo, user);
        
        return fuzzyFindPage(searchPred, pageable);
    }
    
    public Order findOneByOrderId(Integer orderId) 
    {
        return orderDao.findOneByOrderId(orderId);
    }
    
    public OrderVo findNeedToPayByCar(Car car) 
    {
        return orderDao.findTopByCarAndStatusOrderByInTimeDesc(car, OrderStatus.needToPay);
    }
    
    /**
     * 根据设备序列号找到最近识别的订单
     * @param outDeviceSn
     * @return
     */
    public Order findRecentByOutDeviceSn(String outDeviceSn) 
    {
        return orderDao.findTopByOutDeviceSnOrderByOutTimeDesc(outDeviceSn);
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
     */
    public Order processEvent(Event event) throws NoSuchFieldException, SecurityException, ParseException, BusinessException
    {
        Order order = null;
        switch (event.getType())
        {
            case in:  //入场事件,创建订单
                order = carIn(event);
                break;
            case complete: //停车完成,订单计费,完成订单
                order = carComplete(event);
                break;
            case cancel:
                order = eventCancel(event);
                break;
            default:
                break;
        }
        
        return order;
    }
    
    
    /**
     * 停车入场事件处理
     * @param event 事件对象
     * @throws BusinessException 
     */
    public Order carIn(Event event) throws BusinessException 
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
                .build();
        //停车场空位-1
        Integer newAvailableCnt = park.getAvailableCnt() - 1;
        park.setChangeRemark(String.format("车辆入场, 停车场可用车位变化: %d --> %d, 事件: %s", 
                park.getAvailableCnt(), newAvailableCnt, event.getGuid()));
        park.setAvailableCnt(newAvailableCnt);
        parkService.save(park);
        
        //保存 订单
        this.save(order, event);
        return order;
    }
    
    /**
     * 停车完成事件处理
     * @param event 事件对象
     * @throws BusinessException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ParseException 
     */
    public Order carComplete(Event event) throws BusinessException, NoSuchFieldException, SecurityException, ParseException
    {
        Order order = null;
        if (null != event.getActId()) //有事件Id，高位摄像头停车场
        {
            //根据事件Id找入场时生成的的订单
            order = orderDao.findOneByActId(event.getActId());
            
        }
        else //无事件Id，道闸停车场
        {
            //找到最近的入场订单
            order = orderDao.findTopByPlateIdAndStatusOrderByInTimeDesc(event.getPlateId(), OrderStatus.in);
        }
        if (null == order)
        {
            return null;
        }

        //设置出场抓拍设备
        order.setOutDeviceSn(event.getDeviceSn());
        
        //计算并且设置价格
        order.setOutTime(event.getTimeOut());
        this.calAmt(order);
        
        //如果订单价格是0，则直接变成无需支付状态
        if (order.getAmt().equals(BigDecimal.ZERO))
        {
            order.setStatus(OrderStatus.noNeedToPay);
        }
        else
        {
            order.setStatus(OrderStatus.needToPay);//免密支付订单
        }
        
        //停车场空位+1
        Park park = order.getPark();
        Integer newAvailableCnt = park.getAvailableCnt() + 1;
        park.setChangeRemark(String.format("停车完成, 停车场可用车位变化: %d --> %d, 事件: %s", 
                park.getAvailableCnt(), newAvailableCnt, event.getGuid()));
        park.setAvailableCnt(newAvailableCnt);
        parkService.save(park);

        //保存
        this.save(order, event);
        
        return order;
    }
    
    /**
     * 事件取消事件（人工清理时触发）
     * @param event
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    public Order eventCancel(Event event) throws BusinessException, NoSuchFieldException, SecurityException
    {
        //涉及到的订单
        Order order = orderDao.findOneByActId(event.getActId());
        if (null == order)
        {
            return null;
        }
        
        //取消targetEvent
        Park park = order.getPark();
        Event targetEvent = eventService.findOneByGuidAndParkCode(event.getTargetGuid(), park.getCode());
        if (null == targetEvent) //未找到被取消的事件
        {
            return null;
        }
        
        //如果订单已经付款以及后续状态，返回失败
        if (OrderStatus.payed.getValue() <= order.getStatus().getValue())
        {
            String msg = String.format("停车订单【%s】处于【%s】状态, 无法撤销", 
                    order.getCode(), order.getStatus().getText());
            event.setRemark(msg);
            throw new BusinessException(msg);
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
            
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //出场时间为无效时间
            order.setOutTime(new Date(0));
            
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
        return order;
    }
    
    /**
     * 计算订单金额，并且设置到order中
     * @param park
     * @param order
     * @return
     * @throws ParseException 
     * @throws BusinessException 
     */
    public void calAmt(Order order) throws ParseException, BusinessException
    {
        BigDecimal amt = BigDecimal.ZERO;
        //TODO: 计算费用
        Park park = order.getPark();
        Car car = order.getCar();
        DateTime inTime = new DateTime(order.getInTime());
        DateTime outTime = new DateTime(order.getOutTime());
        Integer minutes = Minutes.minutesBetween(inTime, outTime).getMinutes();
        
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
        
        /*
        //月票
        Park park = order.getPark();
        BigDecimal amt = BigDecimal.ZERO;
        //按照出场时间检查是否有月票
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date outDate = sdf.parse(sdf.format(order.getOutTime())); // 去掉时分秒，只保留日期
        Order monthlyTkt = orderDao.findByTypeAndCarCarNoAndParkParkIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                OrderType.monthlyTicket, order.getCar().getCarNo(), 
                order.getPark().getParkId(), OrderStatus.payed, outDate, outDate);
        if (null != monthlyTkt)
        {
            order.setUsedMonthlyTkt(monthlyTkt);
            order.setAmt(BigDecimal.ZERO);
            return;
        }
        //根据停车场规则计算金额
        //计算时间差
        DateTime inTime = new DateTime(order.getInTime());
        DateTime outTime = new DateTime(order.getOutTime());
        int minutes = Minutes.minutesBetween(inTime, outTime).getMinutes();
        
        //计算价格
        BigDecimal feeTimeTotal = new BigDecimal(minutes - park.getFreeTime()); //计算应该计费的时间
        if (0 < feeTimeTotal.compareTo(BigDecimal.ZERO)) //超过免费时间(超过x分钟收费)
        {
            amt = amt.add(park.getPriceLev1()); //第一阶段计费(x分钟后，x分钟--x分钟收费x元)
            
            //第二阶段计费时间(x分钟后，每x分钟收费x元,不足x分钟，按x分钟算)
            if (0 != park.getTimeLev2())
            {
                BigDecimal feeTimeLev2 = feeTimeTotal.subtract(new BigDecimal(park.getTimeLev1())); 
                if (0 < feeTimeLev2.compareTo(BigDecimal.ZERO)) //达到第二阶段计费条件
                {
                    BigDecimal amtLev2 = feeTimeLev2.divide(new BigDecimal(park.getTimeLev2()), 
                            RoundingMode.CEILING).multiply(park.getPriceLev2());
                    amt = amt.add(amtLev2);
                }
            }
        }
        
        //最多不超过x元
        order.setAmt(amt.min(park.getMaxAmt()));
        order.setRealAmt(order.getAmt());*/
        order.setAmt(amt);
        order.setRealAmt(amt);
    }
    
    /**
     * 找到需要支付的停车订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myNeedToPayParking(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwner(OrderType.parking, OrderStatus.needToPay, user, pageable);
    }
    
    /**
     * 找到已经完成支付的停车订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myPayedParking(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwner(OrderType.parking, OrderStatus.payed, user, pageable);
    }
    
    /**
     * 我的钱包变动记录
     * @param userName
     * @return
     */
    public Page<OrderVo> myWalletLogs(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByOwnerAndWalletBalanceIsNotNull(user, pageable);
    }
    
    /**
     * 找到已经完成支付的月票订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myPayedMonthlyTkt(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwner(OrderType.monthlyTicket, OrderStatus.payed, user, pageable);
    }
    
    /**
     * 找到等待付款的月票订单
     * @param userName
     * @return
     */
    public Page<OrderVo> myNeedToPayMonthlyTkt(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByTypeAndStatusAndOwner(OrderType.monthlyTicket, OrderStatus.needToPay, user, pageable);
    }
    
    /**
     * 找到指定用户可以开票的订单
     * @param userName
     * @return
     */
    public Page<OrderVo> invoiceable(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByStatusAndOwnerAndAmtGreaterThanAndInvoiceIsNull(OrderStatus.payed, user, BigDecimal.ZERO, pageable);
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
        //设置支付日期
        order.setPaymentTime(wxPayNotifyParam.getTimeEnd());
        
        //设置付款银行
        order.setBankType(wxPayNotifyParam.getBankType());
        //微信订单号
        order.setWxTransactionId(wxPayNotifyParam.getTransactionId());
        //设置用户关注公众号情况
        order.getOwner().setSubscribe(wxPayNotifyParam.getIsSubscribe());
        
        switch (order.getType())
        {
            case walletIn: //钱包充值订单:增加钱包余额
                User owner = order.getOwner();
                owner.setBalance(owner.getBalance().add(order.getAmt()));
                order.setWalletBalance(owner.getBalance()); //记录钱包余额
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
        if (!order.getStatus().equals(OrderStatus.needToPay))  //已经处理过付款通知(微信会重复推送同一张订单的付款通知)
        {
            return;
        }
        //修改订单状态
        order.setStatus(OrderStatus.needToPay);
        //设置支付日期
        order.setPaymentTime(null);
        
        //TODO:恢复优惠券状态
        
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
                .realAmt(amt)
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
                .realAmt(walletChargeParam.getAmt())
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
        //TODO: 暂定激活优惠券的金额面值的8折
        if (!activeCouponParam.getAmt().setScale(2,BigDecimal.ROUND_HALF_UP).equals(
                coupon.getAmt().multiply(new BigDecimal(ACTIVE_COUPON_FACTOR))
                .setScale(2,BigDecimal.ROUND_HALF_UP)))
        {
            throw new BusinessException("金额不正确");
        }
        
        //检查优惠券金额
        
        String code = util.makeCode(OrderType.coupon);
        
        //创建订单
        Order order = Order.builder()
                .code(code)
                .amt(activeCouponParam.getAmt())
                .realAmt(activeCouponParam.getAmt())
                .activatedCoupon(coupon)
                .type(OrderType.coupon)
                .status(OrderStatus.needToPay)
                .owner(owner).build();
        
        //保存订单
        this.save(order);
        
        //开始付款
        return wxCmpt.unifiedOrder(order);
    }
    
    /*
     * 准备支付, 设置优惠券, 不保存订单
     */
    private void preparePay(Order order, Coupon coupon, String payerName) throws BusinessException 
    {
        //检查订单状态
        if (!order.getStatus().equals(OrderStatus.needToPay))
        {
            throw new BusinessException(String.format("订单: %s 处于: %s 状态, 无需支付", 
                    order.getCode(), order.getStatus().getText()));
        }
        
        //设置无优惠券初始值
        order.setRealAmt(order.getAmt());
        
        if (order.getAutoCoupon()) //自动匹配优惠券
        {
            //找到最合适的优惠券
            coupon = couponService.findBest4Order(order);
        }
        
        //设置coupon
        if (null != coupon)
        {
            //检查coupon状态
            if (!coupon.getStatus().equals(CouponStatus.valid))
            {
                throw new BusinessException(String.format("优惠券处于: %s 状态, 不能使用", coupon.getStatus().getText()));
            }
            
            //检查优惠券拥有者
            if (!coupon.getOwner().getName().equalsIgnoreCase(payerName))
            {
                throw new BusinessException("优惠券在他人名下, 不能使用");
            }
            
            //消耗优惠券
            couponService.useCoupon(coupon, order.getCode());
            order.setUsedCoupon(coupon);
            //设置订单实付款金额
            order.setRealAmt(order.getAmt().subtract(coupon.getAmt()));
            order.appedChangeRemark(String.format("使用优惠券: %s; ", coupon.getCode()));
        }
        save(order);
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
        
        //查找指定优惠券
        Coupon coupon = null;
        if (null != payParam.getCouponId())
        {
            //查找优惠券
            coupon = couponService.findOneById(payParam.getCouponId());
            if (null == coupon)
            {
                throw new BusinessException(String.format("无效的优惠券Id: %d", payParam.getCouponId()));
            }
        }

        //微信无快捷支付
        order.setPaymentType(PaymentType.wx);
        order.setAutoCoupon(false);
        order.setRealAmt(order.getAmt());
        
        //准备支付，检查状态，设置优惠券
        preparePay(order, coupon, payerName);
        
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
        
        //查找指定优惠券
        Coupon coupon = null;
        if (null != payParam.getCouponId())
        {
            //查找优惠券
            coupon = couponService.findOneById(payParam.getCouponId());
            if (null == coupon)
            {
                throw new BusinessException(String.format("无效的优惠券Id: %d", payParam.getCouponId()));
            }
        }
        
        //钱包支付
        order.setPaymentType(PaymentType.qb);
        order.setAutoCoupon(false);
        order.setRealAmt(order.getAmt());
        
        //准备支付，检查状态，设置优惠券
        this.preparePay(order, coupon, payerName);
        
        payByWallet(order);
    }
    
    /**
     * 使用钱包余额付款
     * @param order
     * @throws Exception 
     */
    public void quickPayByWallet(Order order) throws Exception
    {
        //钱包支付
        order.setPaymentType(PaymentType.qb);
        order.setAutoCoupon(true);
        order.setRealAmt(order.getAmt());
        
        //准备支付，检查状态，设置优惠券
        this.preparePay(order, null, order.getOwner().getName());
        
        //使用钱包付款
        this.payByWallet(order);
    }
    
    /**
     * 使用钱包余额付款
     * @param order
     */
    public void payByWallet(Order order) throws BusinessException
    {
        //检查拥有者
        User owner = order.getOwner();
        if (null == owner)
        {
            throw new BusinessException(String.format("订单: %s 是无主, 不能使用钱包支付", order.getCode()));
        }
        
        //余额不足
        if (0 > owner.getBalance().compareTo(order.getRealAmt()))
        {
            throw new BusinessException(String.format("钱包余额: %.2f 元不足, 应付金额: %.2f 元",
                    owner.getBalance().floatValue(), order.getRealAmt().floatValue()));
        }
        
        //扣减钱包余额
        BigDecimal newBlance = owner.getBalance().subtract(order.getRealAmt()).setScale(2, BigDecimal.ROUND_HALF_UP);
        order.appedChangeRemark(String.format("余额: %.2f 元 --> %.2f 元",  owner.getBalance().floatValue(), newBlance.floatValue()));
        owner.setBalance(newBlance);
        
        //订单已经支付
        order.setStatus(OrderStatus.payed);
        order.setPaymentType(PaymentType.qb);
        order.setPaymentTime(new Date());
        order.setWalletBalance(newBlance);
        
        //记录备注
        this.save(order);
    }
}
