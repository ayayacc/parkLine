package com.kl.parkLine.service;

import java.math.BigDecimal;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Dict;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.util.DictCode;

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
    
    /**
     * 处理出入场/停车完成事件
     * @param event 事件对象
     */
    @Transactional
    public void processEvent(Event event)
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
        
        //保存
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
        
        //TODO:计算价格
        order.setAmt(new BigDecimal(10));
        
        //保存
        orderDao.save(order);
    }
    
    /**
     * 事件取消事件（人工清理时触发）
     * @param event
     */
    private void eventCancel(Event event)
    {
        //涉及到的订单
        Order order = orderDao.findOneByActId(event.getActId());
        if (null == order)
        {
            return;
        }

        String targetCode = event.getTargetType().getCode();
        // 取消的是入场事件，取消订单
        if (targetCode.equalsIgnoreCase(DictCode.EVENT_TYPE_CAR_IN))
        {
        }
        
        // 取消的是出场或者停车完成事件
        else if(targetCode.equalsIgnoreCase(DictCode.EVENT_TYPE_CAR_COMPLETE))
        {
            
        }
        
        //订单处于“等待付款状态”, 则将订单改成入场状态
        //如果订单已经付款，返回失败
    }
}
