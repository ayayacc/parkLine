package com.kl.parkLine.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.boyue.BoyueResp;
import com.kl.parkLine.boyue.BoyueRespWrap;
import com.kl.parkLine.component.BoyueEventAdapter;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.service.EventService;
import com.kl.parkLine.service.OrderService;

@RestController
@RequestMapping(value="/boyue")
public class BoyueController
{
    @Autowired
    private BoyueEventAdapter eventAdatper;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private EventService eventService;
    
    /**
     * 接收博粤车牌识别通知
     * @param boyueEvent 博粤事件对象
     * @return
     * @throws SecurityException 
     * @throws IOException 
     * @throws NoSuchFieldException 
     */
    @PostMapping("/plateNotify")
    public BoyueRespWrap plateNotify(@RequestBody BoyueEvent boyueEvent) throws IOException
    {
        BoyueResp resp = null;
        Event event = null;
        try
        {
            //转换并且记录事件
            event = eventAdatper.convert2Event(boyueEvent);
            eventService.save(event);
            
            //处理事件（创建订单或者更改订单状态）
            EventResult eventResult = orderService.processEvent(event);
            
            //转换事件响应
            resp = eventAdatper.convert2EventResp(eventResult);
            
            //设置车牌识别Id
            resp.setPlateId(event.getPlateId());
            
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
        BoyueResp resp = null;
        try
        {
            //处理事件（创建订单或者更改订单状态）
            EventResult eventResult = orderService.processComet(serialno);
            
            //转换事件响应
            resp = eventAdatper.convert2EventResp(eventResult);
        }
        catch (Exception e)
        {
            //发生异常，开闸
            resp = eventAdatper.failedResp(e.getMessage());
        }
        
        BoyueRespWrap boyueRespWrap = new BoyueRespWrap();
        boyueRespWrap.setBoyueResp(resp);
        return boyueRespWrap;
    }
}
