package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.component.EventAdapter;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.OrderLog;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.service.EventService;
import com.kl.parkLine.service.OrderLogService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.xlt.XltEvt;
import com.kl.parkLine.xlt.XltEvtResult;

@RestController
@RequestMapping(value="/MchApi")
public class XltController
{
    @Autowired
    private EventAdapter eventAdatper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private EventService eventService;
    
    @Autowired
    private OrderLogService orderLogService;
    
    /**
     * 接收信路通事件推送
     * @param request 请求消息体
     * @param xltEvt 信路通事件对象
     * @return
     * @throws SecurityException 
     * @throws NoSuchFieldException 
     */
    @PostMapping("/evt/receive")
    public XltEvtResult evtReceive(@RequestBody XltEvt xltEvt) throws NoSuchFieldException, SecurityException
    {
        XltEvtResult result = new XltEvtResult();
        result.setErrorcode(Const.CLT_RET_CODE_OK);
        result.setMessage("");//转换事件对象
        Event event = eventAdatper.xlt2Event(xltEvt);
        try
        {
            //保存事件
            eventService.save(event);
            
            //处理事件（创建订单或者更改订单状态）
            Order order = orderService.processEvent(event);
            
            //无感支付订单
            if (null == order) //空订单
            {
                return result;
            }
            if (!order.getStatus().equals(OrderStatus.needToPay)) //无需支付
            {
                return result;
            }
            if (null == order.getOwner()) //拥有者为空
            {
                return result;
            }
            if (!order.getOwner().getIsQuickPay()) //用户未开通无感支付
            {
                return result;
            }
            try
            {
                orderService.quickPayByWallet(order); //无感支付, 钱包支付订单
            }
            catch (Exception e) //无感支付失败, 记录到订单中
            {
                OrderLog log = OrderLog.builder().order(order).build();
                log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                orderLogService.save(log);
              //TODO: 推送消息到用户
            }
        }
        catch (Exception e)
        {
            event.setRemark(e.getMessage());
            eventService.save(event);
            result.setErrorcode(Const.CLT_RET_CODE_FAILED);
            result.setMessage(e.getMessage());
        }
        return result;
    }
}
