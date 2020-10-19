package com.kl.parkLine.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.OrderPredicates;
import com.kl.parkLine.vo.OrderVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service("orderService")
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
    private JPAQueryFactory jpaQueryFactory;

    /**
     * 分页查询
     * @param searchPred
     * @param pageable
     * @return
     */
    private Page<OrderVo> fuzzyFindPage(Predicate searchPred, Pageable pageable)
    {
        QOrder qOrder = QOrder.order;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qOrder.orderId,
                        qOrder.code,
                        qOrder.status,
                        qOrder.park.name,
                        qOrder.park.parkId,
                        qOrder.car.carId,
                        qOrder.car.carNo,
                        qOrder.type
                )
                .from(qOrder)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        
        //转换成vo
        List<OrderVo> orderVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> OrderVo.builder()
                        .orderId(tuple.get(qOrder.orderId))
                        .code(tuple.get(qOrder.code))
                        .type(tuple.get(qOrder.type))
                        .parkParkId(tuple.get(qOrder.park.parkId))
                        .parkName(tuple.get(qOrder.park.name))
                        .carCarId(tuple.get(qOrder.car.carId))
                        .carCarNo(tuple.get(qOrder.car.carNo))
                        .status(tuple.get(qOrder.status))
                        .build()
                        )
                .collect(Collectors.toList());
        
        return new PageImpl<>(orderVos, pageable, queryResults.getTotal());
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
     * 处理出入场/停车完成事件
     * @param event 事件对象
     */
    @Transactional
    public void processEvent(Event event) throws BusinessException
    {
        //保存event
        eventService.save(event);
        
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
     * 构建订单编码
     * @param type
     * @return 订单编码
     */
    private String makeCode(OrderType type)
    {
        Date now = new Date();
        String prefix = "";
        switch (type)
        {
            case parking:  //停车
                prefix = "TC";
                break;
            case monthlyTicket: //月票
                prefix = "YP";
                break;
            case coupon:  //优惠券
                prefix = "YHQ";
                break;
            case walletIn: //钱包充值
                prefix = "CZ";
                break;
            default:
                break;
        }
        String code = prefix + String.valueOf(now.getTime());
        return code;
    }
    
    /**
     * 停车入场事件处理
     * @param event 事件对象
     */
    private void carIn(Event event) 
    {
        Order order = new Order();
        //订单编码
        order.setCode(makeCode(OrderType.parking));
        //车辆信息
        Car car = carService.getCar(event.getPlateNo());
        order.setCar(car);
        //停车订单类型
        order.setType(OrderType.parking);
        //入场状态
        order.setStatus(OrderStatus.in);
        //停车场
        Park park = parkService.findOneByCode(event.getParkCode());
        order.setPark(park);
        //事件ID
        order.setActId(event.getActId());
        //入场时间
        order.setInTime(event.getTimeIn());
        
        //停车场空位-1
        park.setAvailableCnt(park.getAvailableCnt()-1);
        
        //保存 订单
        orderDao.save(order);
    }
    
    /**
     * 停车完成事件处理
     * @param event 事件对象
     */
    private void carComplete(Event event)
    {
        //根据事件Id找入场时生成的的订单
        Order order = orderDao.findOneByActId(event.getActId());
        
        //状态：等待支付ORDER_STATUS_NEED_TO_PAY
        order.setStatus(OrderStatus.needToPay);
        
        //记录出厂时间
        order.setOutTime(event.getTimeOut());
        
        //计算并且设置价格
        this.calAmt(order);
        
        //如果订单价格是0，则直接变成已经支付状态
        if (order.getAmt().equals(BigDecimal.ZERO))
        {
            order.setStatus(OrderStatus.noNeedToPay);
        }
        
        //停车场空位+1
        Park park = order.getPark();
        park.setAvailableCnt(park.getAvailableCnt() + 1);

        //保存
        orderDao.save(order);
    }
    
    /**
     * 事件取消事件（人工清理时触发）
     * @param event
     */
    private void eventCancel(Event event) throws BusinessException
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
        
        //如果订单已经付款以及后续状态，返回失败
        if (OrderStatus.payed.getValue() <= order.getStatus().getValue())
        {
            throw new BusinessException(String.format("停车订单【%s】处于【%s】状态, 无法撤销", 
                    order.getCode(), order.getStatus().getText()));
        }

        // 取消的是入场事件，取消订单
        if (EventType.in.getValue() == event.getTargetType().getValue())
        {
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //当前订单状态是入场，停车场空位数量+1
            
            if (OrderStatus.in.getValue() == order.getStatus().getValue())
            {
                park.setAvailableCnt(park.getAvailableCnt() + 1);
            }
            
            //取消订单
            order.setStatus(OrderStatus.canceled);
        }
        // 取消的是出场或者停车完成事件
        else if(EventType.complete.getValue() == event.getTargetType().getValue())
        {
            //将订单改成入场状态
            order.setStatus(OrderStatus.in);
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //停车场空位数量-1
            park.setAvailableCnt(park.getAvailableCnt() - 1);
        }
        
        //保存订单
        orderDao.save(order);
        
        //禁用目标事件
        targetEvent.setEnabled("N");
        eventService.save(targetEvent);
    }
    
    /**
     * 计算订单金额，并且设置到order中
     * @param park
     * @param order
     * @return
     */
    @Transactional(readOnly = true)
    public void calAmt(Order order)
    {
        Park park = order.getPark();
        BigDecimal amt = BigDecimal.ZERO;
        //TODO:检查是否月票订单
        
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
        return orderDao.findByStatusAndOwnerAndAmtGreaterThan(OrderStatus.needToPay, user, BigDecimal.ZERO, pageable);
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
        return orderDao.findByStatusAndOwnerAndAmtGreaterThan(OrderStatus.payed, user, BigDecimal.ZERO, pageable);
    }
    
    /**
     * 将车辆涉及的无主订单设置拥有者
     * @param car 被绑定用户的车辆
     */
    @Transactional
    public void setOrderOwnerByCar(Car car) 
    {
        //找到指定车牌号的无主订单
        Set<Order> orders = orderDao.findByCarAndOwnerIsNull(car);
        
        //设置拥有者
        for (Order order : orders)
        {
            order.setOwner(car.getUser());
        }
        
        //保存订单
        orderDao.saveAll(orders);
    }
    
}
