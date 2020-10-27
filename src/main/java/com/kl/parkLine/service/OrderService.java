package com.kl.parkLine.service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.component.Utils;
import com.kl.parkLine.component.WxCmpt;
import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Coupon;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.OrderLog;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CouponStatus;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.CreateMonthlyTktParam;
import com.kl.parkLine.json.WxPayNotifyParam;
import com.kl.parkLine.json.WxunifiedOrderResult;
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
    private Utils util;
    
    @Autowired
    private WxCmpt wxCmpt;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    private final List<OrderStatus> checkedStatus = new ArrayList<OrderStatus>();
    
    public OrderService()
    {
        //检查重复订单包含的状态: 存在等待付款和已经付款的月票订单时，不能再次创建重复的月票
        checkedStatus.add(OrderStatus.needToPay);
        checkedStatus.add(OrderStatus.payed);
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
        QueryResults<OrderVo> queryResults = jpaQueryFactory
                .select(Projections.bean(OrderVo.class, qOrder.orderId,
                        qOrder.code,
                        qOrder.status,
                        qOrder.park.name.as("parkName"),
                        qOrder.park.parkId.as("parkParkId"),
                        qOrder.car.carId.as("carCarId"),
                        qOrder.car.carNo.as("carCarNo"),
                        qOrder.type))
                .from(qOrder)
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
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
    public Page<OrderVo> fuzzyFindPageAsManager(OrderVo orderVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        
        Predicate searchPred = orderPredicates.fuzzyAsManager(orderVo, user);
        
        return fuzzyFindPage(searchPred, pageable);
    }
    
    @Transactional
    public Order findOneByOrderId(Integer orderId) 
    {
        return orderDao.findOneByOrderId(orderId);
    }
    
    /**
     * 保存一个订单
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    @Transactional
    public void save(Order order) throws BusinessException
    {
        this.save(order, null);
    }
    
    /**
     * 保存一个订单
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    @Transactional
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
            if (!StringUtils.isEmpty(order.getDiff()))
            {
                diff = order.getDiff();
            }
            else
            {
                diff = util.difference(orderDst.get(), order);
            }
            
            BeanUtils.copyProperties(order, orderDst.get(), util.getNullPropertyNames(order));
            
            order = orderDst.get();
        }
        
        //保存数据
        OrderLog log = new OrderLog();
        log.setDiff(diff);
        log.setRemark(order.getChangeRemark());
        log.setOrder(order);
        log.setEvent(event);
        order.getLogs().add(log);
        orderDao.save(order);
    }
    
    /**
     * 处理出入场/停车完成事件
     * @param event 事件对象
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ParseException 
     */
    @Transactional
    public void processEvent(Event event) throws BusinessException, NoSuchFieldException, SecurityException, ParseException
    {
        switch (event.getType())
        {
            case in:  //入场事件,创建订单
                carIn(event);
                break;
            case complete: //停车完成,订单计费,完成订单
                carComplete(event);
                break;
            case cancel:
                eventCancel(event);
                break;
            default:
                break;
        }
        
        return;
    }
    
    
    /**
     * 停车入场事件处理
     * @param event 事件对象
     * @throws BusinessException 
     */
    private void carIn(Event event) throws BusinessException 
    {
        //停车场
        Park park = parkService.findOneByCode(event.getParkCode());
        
        //车辆信息
        Order order = Order.builder()
                .code(util.makeCode(OrderType.parking))
                .car(carService.getCar(event.getPlateNo()))
                .type(OrderType.parking)
                .status(OrderStatus.in)
                .park(park)
                .actId(event.getActId())
                .inTime(event.getTimeIn())
                .build();
        //停车场空位-1
        park.setAvailableCnt(park.getAvailableCnt()-1);
        
        //保存 订单
        this.save(order, event);
    }
    
    /**
     * 停车完成事件处理
     * @param event 事件对象
     * @throws BusinessException 
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     * @throws ParseException 
     */
    private void carComplete(Event event) throws BusinessException, NoSuchFieldException, SecurityException, ParseException
    {
        //根据事件Id找入场时生成的的订单
        Order order = orderDao.findOneByActId(event.getActId());
        if (null == order)
        {
            return;
        }
        StringBuilder difference = new StringBuilder();
        //记录出场时间
        Field field = order.getClass().getDeclaredField("outTime");
        NeedToCompare antNeedToCompare = field.getAnnotation(NeedToCompare.class); 
        difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", antNeedToCompare.name(),
                util.formatValue(order.getOutTime(), field),
                util.formatValue(event.getTimeOut(), field)));
        order.setOutTime(event.getTimeOut());
        
        //计算并且设置价格
        BigDecimal oldAmt = order.getAmt();
        this.calAmt(order);
        field = order.getClass().getDeclaredField("amt");
        antNeedToCompare = field.getAnnotation(NeedToCompare.class); 
        difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", antNeedToCompare.name(),
                util.formatValue(oldAmt, field),
                util.formatValue(order.getAmt(), field)));
        
        //如果订单价格是0，则直接变成无需支付状态
        OrderStatus oldStatus = order.getStatus();
        field = order.getClass().getDeclaredField("status");
        antNeedToCompare = field.getAnnotation(NeedToCompare.class); 
        if (order.getAmt().equals(BigDecimal.ZERO))
        {
            order.setStatus(OrderStatus.noNeedToPay);
        }
        else
        {
            order.setStatus(OrderStatus.needToPay);
        }
        difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", antNeedToCompare.name(),
                util.formatValue(oldStatus, field),
                util.formatValue(order.getStatus(), field)));
        
        order.setDiff(String.format("<ol>%s</ol>", difference.toString()));
        
        //停车场空位+1
        Park park = order.getPark();
        park.setAvailableCnt(park.getAvailableCnt() + 1);

        //保存
        this.save(order, event);
    }
    
    /**
     * 事件取消事件（人工清理时触发）
     * @param event
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    private void eventCancel(Event event) throws BusinessException, NoSuchFieldException, SecurityException
    {
        //涉及到的订单
        Order order = orderDao.findOneByActId(event.getActId());
        if (null == order)
        {
            return;
        }
        
        //取消targetEvent
        Park park = order.getPark();
        Event targetEvent = eventService.findOneByGuidAndParkCode(event.getTargetGuid(), park.getCode());
        if (null == targetEvent) //未找到被取消的事件
        {
            return;
        }
        
        StringBuilder difference = new StringBuilder();
        //如果订单已经付款以及后续状态，返回失败
        if (OrderStatus.payed.getValue() <= order.getStatus().getValue())
        {
            String msg = String.format("停车订单【%s】处于【%s】状态, 无法撤销", 
                    order.getCode(), order.getStatus().getText());
            event.setRemark(msg);
            throw new BusinessException(msg);
        }

        BigDecimal oldAmt = order.getAmt();
        Field fAmt = order.getClass().getDeclaredField("amt");
        OrderStatus oldStatus = order.getStatus();
        Field fStatus = order.getClass().getDeclaredField("status");
        Date oldOutTime = order.getOutTime();
        Field fOutTime = order.getClass().getDeclaredField("outTime");
        
        // 取消的是入场事件，取消订单
        if (EventType.in.getValue() == event.getTargetType().getValue())
        {
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", 
                    fAmt.getAnnotation(NeedToCompare.class).name(),
                    util.formatValue(oldAmt, fAmt),
                    util.formatValue(order.getAmt(), fAmt)));
            
            //当前订单状态是入场，停车场空位数量+1
            
            if (OrderStatus.in.getValue() == order.getStatus().getValue())
            {
                park.setAvailableCnt(park.getAvailableCnt() + 1);
            }
            
            //取消订单
            order.setStatus(OrderStatus.canceled);
            difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", 
                    fStatus.getAnnotation(NeedToCompare.class).name(),
                    util.formatValue(oldStatus, fStatus),
                    util.formatValue(order.getStatus(), fStatus)));
        }
        // 取消的是出场或者停车完成事件
        else if(EventType.complete.getValue() == event.getTargetType().getValue())
        {
            //将订单改成入场状态
            order.setStatus(OrderStatus.in);
            difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", 
                    fStatus.getAnnotation(NeedToCompare.class).name(),
                    util.formatValue(oldStatus, fStatus),
                    util.formatValue(order.getStatus(), fStatus)));
            
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", 
                    fAmt.getAnnotation(NeedToCompare.class).name(),
                    util.formatValue(oldAmt, fAmt),
                    util.formatValue(order.getAmt(), fAmt)));
            
            //出场时间为空
            order.setOutTime(null);
            difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", 
                    fOutTime.getAnnotation(NeedToCompare.class).name(),
                    util.formatValue(oldOutTime, fOutTime),
                    util.formatValue(order.getOutTime(), fOutTime)));
            
            //停车场空位数量-1
            park.setAvailableCnt(park.getAvailableCnt() - 1);
        }
        
        order.setDiff(String.format("<ol>%s</ol>", difference.toString()));
        
        //保存订单
        this.save(order, event);
        
        //禁用目标事件
        targetEvent.setEnabled("N");
        eventService.save(targetEvent);
    }
    
    /**
     * 计算订单金额，并且设置到order中
     * @param park
     * @param order
     * @return
     * @throws ParseException 
     */
    @Transactional(readOnly = true)
    public void calAmt(Order order) throws ParseException
    {
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
    }
    
    /**
     * 找到指定用户可以开票的订单
     * @param userName
     * @return
     */
    @Transactional(readOnly = true)
    public Page<OrderVo> needToPay(String userName, Pageable pageable)
    {
        User user = userService.findByName(userName);
        return orderDao.findByStatusAndOwnerAndAmtGreaterThanAndInvoiceIsNull(OrderStatus.needToPay, user, BigDecimal.ZERO, pageable);
    }
    
    /**
     * 找到指定用户可以开票的订单
     * @param userName
     * @return
     */
    @Transactional(readOnly = true)
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
    @Transactional
    public void setOrderOwnerByCar(Car car) throws BusinessException 
    {
        //找到指定车牌号的无主订单
        Set<Order> orders = orderDao.findByCarAndOwnerIsNull(car);
        
        //设置拥有者
        for (Order order : orders)
        {
            order.setOwner(car.getUser());
            this.save(order);
        }
    }
    
    /**
     * 处理订单支付结果
     * @param wxPayNotifyParam
     * @throws BusinessException 
     */
    public void wxPaySuccess(WxPayNotifyParam wxPayNotifyParam) throws BusinessException
    {
        //找到付款订单
        Order order = orderDao.findOneByCode(wxPayNotifyParam.getOutTradeNo());
        if (null == order)
        {
            return;
        }
        if (null != order.getPaymentTime())  //已经处理过付款通知(微信会重复推送同一张订单的付款通知)
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
        
        //设置使用的优惠券状态
        if (null != order.getUsedCoupon())
        {
            order.getUsedCoupon().setStatus(CouponStatus.used);
        }
        
        switch (order.getType())
        {
            case walletIn: //钱包充值订单:增加钱包余额
                User owner = order.getOwner();
                owner.setBalance(owner.getBalance().add(order.getAmt()));
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
        
        this.save(order);
    }
    
    /**
     * 检查月票订单参数有效性,开始结束日期是否在月头月尾,价格是否正确
     * @param park
     * @return
     */
    private void checkMonthlyTktParams(Park park, Date startDate, Date endDate, BigDecimal amt) throws BusinessException
    {
        //检查开始结束日期是否在月头月尾
        DateTime dateTimeStart = new DateTime(startDate);
        if (1 != dateTimeStart.getDayOfMonth()) //不是月初第一天
        {
            throw new BusinessException("开始日期应该为月初第一天");
        }
        DateTime dateTimeEnd = new DateTime(endDate);
        if (1 != dateTimeEnd.plusDays(1).getDayOfMonth()) //不是月末最后一天
        {
            throw new BusinessException("结束日期应该为月末最后一天");
        }
        
        //检查日期大小
        if (dateTimeStart.isAfter(dateTimeEnd))
        {
            throw new BusinessException("开始日期必须小于结束日期");
        }
        
        //检查价格
        int month = (dateTimeEnd.getYear()-dateTimeStart.getYear())*12 + dateTimeEnd.getMonthOfYear()-dateTimeStart.getMonthOfYear() + 1;
        if (!amt.equals(park.getMonthlyPrice().multiply(new BigDecimal(month))))
        {
            throw new BusinessException("传递的月票价格不正确,请刷新价格后重试");
        }
    }
    
    private Boolean existingValid(Car car, Park park, Date startDate, Date endDate)
    {
        return orderDao.existsByTypeAndCarCarNoAndParkParkIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual
                (OrderType.monthlyTicket, car.getCarNo(), park.getParkId(), checkedStatus, endDate, startDate);
    }
    
    /**
     * 创建一个新的月票订单
     * @param monthlyTktParam
     * @throws Exception 
     */
    @Transactional
    public WxunifiedOrderResult createMonthlyTkt(CreateMonthlyTktParam monthlyTktParam, String userName) throws Exception
    {
        User owner = userService.findByName(userName);
        Park park = parkService.findOneById(monthlyTktParam.getParkId());
        
        //检查月票订单参数
        this.checkMonthlyTktParams(park, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate(), monthlyTktParam.getAmt());
        

        Car car = carService.getCar(monthlyTktParam.getCarNo());
        //检查是否有重复的月票订单
        if (this.existingValid(car, park, monthlyTktParam.getStartDate(), monthlyTktParam.getEndDate()))
        {
            throw new BusinessException("请勿重复购买月票");
        }
        
        String code = util.makeCode(OrderType.monthlyTicket);
        
        //创建订单
        Order order = Order.builder()
                .code(code).car(car).park(park).amt(monthlyTktParam.getAmt())
                .type(OrderType.monthlyTicket)
                .startDate(monthlyTktParam.getStartDate())
                .endDate(monthlyTktParam.getEndDate())
                .status(OrderStatus.needToPay)
                .owner(owner).build();
        
        //保存订单
        this.save(order);
        
        //开始付款
        return wxCmpt.unifiedOrder(order);
    }
}
