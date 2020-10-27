package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.component.EventAdapter;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.service.EventService;
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
            //处理事件（创建订单或者更改订单状态）
            orderService.processEvent(event);
        }
        catch (BusinessException e)
        {
            result.setErrorcode(Const.CLT_RET_CODE_FAILED);
            result.setMessage(e.getMessage());
        }
        finally
        {
            eventService.save(event);
        }
        
        return result;
    }
}
