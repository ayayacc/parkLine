package com.kl.parkLine.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Dict;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.QOrder;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.OrderPredicates;
import com.kl.parkLine.util.DictCode;
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
    private CarService carService;
    
    @Autowired
    private DictService dictService;
    
    @Autowired
    private ParkService parkService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    @Transactional(readOnly = true)
    public Page<OrderVo> fuzzyFindPage(Order order, Pageable pageable, Authentication auth)
    {
        User user = userService.findByName(auth.getName());
        Predicate searchPred = OrderPredicates.fuzzySearch(order, user);
        
        QOrder qOrder = QOrder.order;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qOrder.orderId,
                        qOrder.code,
                        qOrder.status.text,
                        qOrder.type.text
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
                        .type(tuple.get(qOrder.type.text))
                        .status(tuple.get(qOrder.status.text))
                        .build()
                        )
                .collect(Collectors.toList());
        
        return new PageImpl<>(orderVos, pageable, queryResults.getTotal());
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
        
        switch (event.getType().getCode())
        {
            case DictCode.EVENT_TYPE_CAR_IN:  //入场事件,创建订单
                carIn(event);
                break;
            case DictCode.EVENT_TYPE_CAR_COMPLETE: //停车完成,订单计费,完成订单
                carComplete(event);
                break;
            case DictCode.EVENT_TYPE_CANCEL:
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
    private String makeCode(String type)
    {
        Date now = new Date();
        String prefix = "";
        switch (type)
        {
            case DictCode.ORDER_TYPE_PARK:  //停车
                prefix = "TC";
                break;
            case DictCode.ORDER_TYPE_MONTHLY_TICKET: //月票
                prefix = "YP";
                break;
            case DictCode.ORDER_TYPE_COUPON:  //优惠券
                prefix = "YHQ";
                break;
            case DictCode.ORDER_TYPE_WALLET_IN: //钱包充值
                prefix = "CZ";
                break;
            case DictCode.ORDER_TYPE_WALLET_OUT: //钱包提现
                prefix = "TX";
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
        order.setCode(makeCode(DictCode.EVENT_TYPE_CAR_IN));
        //车辆信息
        Car car = carService.getCar(event.getPlateNo());
        order.setCar(car);
        //停车订单类型
        Dict dict = dictService.findOneByCode(DictCode.ORDER_TYPE_PARK);  
        order.setType(dict);
        //入场状态
        dict = dictService.findOneByCode(DictCode.ORDER_STATUS_IN);
        order.setStatus(dict);
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
        Dict dict = dictService.findOneByCode(DictCode.ORDER_STATUS_NEED_TO_PAY);
        order.setStatus(dict);
        
        //记录出厂时间
        order.setOutTime(event.getTimeOut());
        
        //计算并且设置价格
        this.calAmt(order);
        
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
        Dict status = dictService.findOneByCode(DictCode.ORDER_STATUS_PAYED);
        if (0 <= order.getStatus().getSortIdx().compareToIgnoreCase(status.getSortIdx()))
        {
            throw new BusinessException(String.format("停车订单【%s】处于【%s】状态, 无法撤销", 
                    order.getCode(), order.getStatus().getText()));
        }

        String targetCode = event.getTargetType().getCode();
        // 取消的是入场事件，取消订单
        if (targetCode.equalsIgnoreCase(DictCode.EVENT_TYPE_CAR_IN))
        {
            //金额为0
            order.setAmt(BigDecimal.ZERO);
            
            //当前订单状态是入场，停车场空位数量+1
            if (order.getStatus().getCode().equalsIgnoreCase(DictCode.ORDER_STATUS_IN))
            {
                park.setAvailableCnt(park.getAvailableCnt() + 1);
            }
            
            //取消订单
            status = dictService.findOneByCode(DictCode.ORDER_STATUS_CANCELED);
            order.setStatus(status);
        }
        // 取消的是出场或者停车完成事件
        else if(targetCode.equalsIgnoreCase(DictCode.EVENT_TYPE_CAR_COMPLETE))
        {
            //将订单改成入场状态
            status = dictService.findOneByCode(DictCode.ORDER_STATUS_IN);
            order.setStatus(status);
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
        //TODO：检查是否月票订单
        
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
    
}