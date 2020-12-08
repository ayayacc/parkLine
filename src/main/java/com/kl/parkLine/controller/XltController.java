package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.component.XltEventAdapter;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.service.EventService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.xlt.XltEvt;
import com.kl.parkLine.xlt.XltEvtResult;

@RestController
@RequestMapping(value="/MchApi")
public class XltController
{
    @Autowired
    private XltEventAdapter eventAdatper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private EventService eventService;
    
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
        XltEvtResult resp = null;
        Event event = null;
        try
        {
            //转换并且记录事件
            event = eventAdatper.convert2Event(xltEvt);
            eventService.save(event);
            
            //处理事件（创建订单或者更改订单状态）
            EventResult eventResult = orderService.processEvent(event);
            
            //转换事件响应
            resp = eventAdatper.convert2EventResp(eventResult);
            
        }
        catch (Exception e)
        {
            //发生异常，开闸
            resp = eventAdatper.failedResp(e.getMessage());
            if (null != event)
            {
                event.setRemark(e.getMessage());
                eventService.save(event);
            }
        }
        return resp;
    }
}
