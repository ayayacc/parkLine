package com.kl.parkLine.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.boyue.BoyueResp;
import com.kl.parkLine.boyue.BoyueRespWrap;
import com.kl.parkLine.component.EventAdapter;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.OrderLog;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.exception.EventException;
import com.kl.parkLine.service.EventService;
import com.kl.parkLine.service.OrderLogService;
import com.kl.parkLine.service.OrderService;

@RestController
@RequestMapping(value="/boyue")
public class BoyueController
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
     * 接收博粤车牌识别通知
     * @param boyueEvent 博粤事件对象
     * @return
     * @throws SecurityException 
     * @throws IOException 
     * @throws NoSuchFieldException 
     */
    @PostMapping("/plateNotify")
    public BoyueRespWrap plateNotify(@RequestBody BoyueEvent boyueEvent) throws SecurityException, IOException
    {
        BoyueResp resp = new BoyueResp();
        Event event = null;
        Date now = new Date();
        try
        {
            //转换事件
            event = eventAdatper.boyue2Event(boyueEvent);
            
            //保存事件
            eventService.save(event);
            
            //处理事件（创建订单或者更改订单状态）
            Order order = null;
            String content = "";
            try
            {
                order = orderService.processEvent(event);
            }
            catch (EventException e) //识别黑名单车辆和欠费车辆
            {
                content = e.getMessage();
            }
            
            resp.setPlateId(event.getPlateId());
            //入场事件,开闸
            if (event.getType().equals(EventType.in))
            {
                if (null == order)  //黑名单或者欠费车辆
                {
                    resp.setInfo("notok"); //回复notok不开闸
                    resp.setContent(content);
                }
                else
                {
                    resp.setInfo("ok"); //回复OK开闸
                    resp.setContent(String.format("欢迎光临:%s", event.getPlateNo()));
                }
            }
            else if (event.getType().equals(EventType.complete)) //停车完成，出场
            {
                if (null == order) //订单为空,发生异常,可能入场时车牌识别错误
                {
                    resp.setInfo("ok"); //回复OK开闸
                    resp.setContent(String.format("一路顺风:%s", event.getPlateNo()));
                }
                else  //订单不为空
                {
                    if (order.getStatus().equals(OrderStatus.noNeedToPay)) //无需支付 
                    {
                        resp.setInfo("ok"); //回复OK开闸
                        resp.setContent(String.format("一路顺风:%s", event.getPlateNo()));
                        orderService.setOut(order, true);
                    }
                    else if (order.getStatus().equals(OrderStatus.payed)) //已经提前支付
                    {
                        //未超过离场时间限制
                        if (!now.after(order.getOutTimeLimit()))
                        {
                            resp.setInfo("ok"); //回复OK开闸
                            resp.setContent(String.format("一路顺风:%s", event.getPlateNo()));
                            orderService.setOut(order, true);
                        }
                        else //超过离场时间限制
                        {
                            //计算需要补缴的费用
                            orderService.resetAmtAndOutTimeLimit(order);
                            if (order.getStatus().equals(OrderStatus.needToPay))  //产生新的费用
                            {
                                if (order.getOwner().getIsQuickPay())  //开通了无感支付
                                {
                                    try
                                    {
                                        orderService.quickPayByWallet(order); //无感支付, 钱包支付订单
                                        resp.setInfo("ok"); //回复OK开闸
                                        resp.setContent(String.format("一路顺风:%s", event.getPlateNo()));
                                        orderService.setOut(order, true);
                                    }
                                    catch (Exception e) //无感支付失败, 记录到订单中
                                    {
                                        BigDecimal unPayedAmt = order.getAmt().subtract(order.getPayedAmt());
                                        resp.setInfo("notok"); //回复notok不开闸,等待用户补交费
                                        resp.setContent(String.format("请补交费%.2f元", unPayedAmt.floatValue()));
                                        OrderLog log = OrderLog.builder().order(order).build();
                                        log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                                        orderLogService.save(log);
                                    }
                                }
                            }
                            else
                            {
                                resp.setInfo("ok"); //回复OK开闸
                                resp.setContent(String.format("一路顺风:%s", event.getPlateNo()));
                                orderService.setOut(order, true);
                            }
                        }
                    }
                    else if (order.getStatus().equals(OrderStatus.needToPay)) //需要付款
                    {
                        DateTime inTime = new DateTime(order.getInTime());
                        DateTime outTime = new DateTime(order.getOutTime());
                        Period period = new Period(inTime, outTime, PeriodType.time());
                        if (null == order.getOwner()
                            ||!order.getOwner().getIsQuickPay()) //拥有者为空或者用户未开通无感支付
                         {
                             resp.setInfo("notok"); //回复notok开闸,等待用户付款
                             resp.setContent(String.format("停车时长%d小时%d分,请交费%.2f元", period.getHours(),
                                     period.getMinutes(), order.getAmt().floatValue()));
                         }
                         else //用户开通了无感支付
                         {
                             try
                             {
                                 orderService.quickPayByWallet(order); //无感支付, 钱包支付订单
                                 resp.setInfo("ok"); //回复OK开闸
                                 resp.setContent(String.format("一路顺风:%s", event.getPlateNo()));
                                 orderService.setOut(order, true);
                             }
                             catch (Exception e) //无感支付失败, 记录到订单中
                             {
                                 resp.setInfo("notok"); //回复notok开闸,等待用户付款
                                 resp.setContent(String.format("停车时长%d小时%d分,请交费%.2f元", period.getHours(),
                                         period.getMinutes(), order.getAmt().floatValue()));
                                 OrderLog log = OrderLog.builder().order(order).build();
                                 log.setRemark(String.format("%s, 无感支付失败: %s", order.getChangeRemark(), e.getMessage()));
                                 orderLogService.save(log);
                             }
                         }
                    } 
                }
            }
        }
        catch (Exception e)
        {
            //发生异常，开闸
            resp.setInfo("ok"); //回复OK开闸
            event.setRemark(e.getMessage());
            eventService.save(event);
        }
        
        BoyueRespWrap boyueRespWrap = new BoyueRespWrap();
        boyueRespWrap.setBoyueResp(resp);
        return boyueRespWrap;
    }
    
    /**
     * comet 轮询
     * @param request 请求消息体
     * @return
     * @throws BusinessException 
     * @throws SecurityException 
     */
    @PostMapping("/comet")
    public BoyueRespWrap comet(@RequestParam(value="serialno") String serialno) throws BusinessException
    {
        //根据摄像头找到对应的订单
        Date now = new Date();
        BoyueResp resp = new BoyueResp();
        resp.setInfo("notok");
        //根据订单情况控制是否开闸
        Order order = orderService.findLastNotOutByOutDeviceSn(serialno); //找到最近的车辆未开闸出场订单
        if (null == order) //未找到订单，不开闸
        {
            BoyueRespWrap boyueRespWrap = new BoyueRespWrap();
            boyueRespWrap.setBoyueResp(resp);
            return boyueRespWrap;
        }
        else 
        {
            if (order.getStatus().equals(OrderStatus.noNeedToPay)) //无需付款，开闸
            {
                resp.setInfo("ok");
                resp.setContent("一路顺风");
                orderService.setOut(order, true);
            }
            else if (order.getStatus().equals(OrderStatus.payed)) //已经支付
            {
                //未超过出场时限
                if (!now.after(order.getOutTimeLimit()))
                {
                    resp.setInfo("ok");
                    resp.setContent("一路顺风");
                    orderService.setOut(order, true);
                }
            }

            BoyueRespWrap boyueRespWrap = new BoyueRespWrap();
            boyueRespWrap.setBoyueResp(resp);
            return boyueRespWrap;
        }
    }
}
