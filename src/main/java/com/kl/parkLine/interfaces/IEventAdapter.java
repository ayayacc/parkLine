package com.kl.parkLine.interfaces;

import java.io.UnsupportedEncodingException;

import com.kl.parkLine.entity.Event;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.EventResult;

public interface IEventAdapter<REQ, RES>
{
    /**
     * 将各种设备格式的事件通知转换成内部格式
     * @param otherEvent
     * @return
     * @throws BusinessException
     */
    public Event convert2Event(REQ otherEvent) throws BusinessException;
    
    /**
     * 将内部格式的事件响应转换成各种设备格式
     * @param eventResult
     * @return
     * @throws UnsupportedEncodingException 
     */
    public RES convert2EventResp(EventResult eventResult) throws UnsupportedEncodingException;
    
    /**
     * 事件处理错误时返回给设备的响应
     * @param messge
     * @return
     */
    public RES failedResp(String messge);
}
