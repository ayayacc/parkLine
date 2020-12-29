package com.kl.parkLine.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.EventPic;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.ParkAbnormal;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.interfaces.IEventAdapter;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.xlt.XltEvt;
import com.kl.parkLine.xlt.XltEvtResult;

/**
 * @author chenc
 */
@Component
public class XltEventAdapter implements IEventAdapter<XltEvt, XltEvtResult>
{
    private final  String DEVICE_COMPANY_XLT = "信路通";
    
    //信路通返回值
    private final Integer CLT_RET_CODE_OK = 0; //成功
    private final Integer CLT_RET_CODE_FAILED = 3; //执行失败
    
    //事件类型
    private final String XLT_EVENT_CAR_IN = "evt.car.in";
    private final String XLT_EVENT_CAR_OUT = "evt.car.out";
    private final String XLT_EVENT_CAR_COMPLETE = "evt.car.complete";
    private final String XLT_EVENT_CANCEL = "evt.cancel";
    //事件映射表
    private final Map<String, EventType> mapXltEventTypeToEnum = new HashMap<String, EventType>();
    
    //车牌颜色, 未知:0,蓝:1,黄:2, 黑:3,白:4, 绿:5
    private final Integer XLT_PLATE_COLOR_UNKNOWN = 0;
    private final Integer XLT_PLATE_COLOR_BLUE = 1;
    private final Integer XLT_PLATE_COLOR_YELLOW = 2;
    private final Integer XLT_PLATE_COLOR_BLACK = 3;
    private final Integer XLT_PLATE_COLOR_WHITE = 4;
    private final Integer XLT_PLATE_COLOR_GREEN = 5;
    //车牌颜色映射表
    private final Map<Integer, PlateColor> mapXltPalteColorToEnum = new HashMap<Integer, PlateColor>();
    
    //停车异常信息
    private final Integer XLT_PARK_ABNORMAL_ZC = 0;
    private final Integer XLT_PARK_ABNORMAL_KW = 1;
    private final Integer XLT_PARK_ABNORMAL_XW = 2;
    private final Integer XLT_PARK_ABNORMAL_YX = 3;
    //停车异常映射表
    private final Map<Integer, ParkAbnormal> mapXltParkAbnormalToEnum = new HashMap<Integer, ParkAbnormal>();
    
    public XltEventAdapter()
    {
        //事件映射表
        mapXltEventTypeToEnum.put(XLT_EVENT_CAR_IN, EventType.in);
        mapXltEventTypeToEnum.put(XLT_EVENT_CAR_OUT, EventType.out);
        mapXltEventTypeToEnum.put(XLT_EVENT_CAR_COMPLETE, EventType.complete);
        mapXltEventTypeToEnum.put(XLT_EVENT_CANCEL, EventType.cancel);
        
        //信路通车牌颜色映射表
        mapXltPalteColorToEnum.put(XLT_PLATE_COLOR_UNKNOWN, PlateColor.unknown);
        mapXltPalteColorToEnum.put(XLT_PLATE_COLOR_BLUE, PlateColor.blue);
        mapXltPalteColorToEnum.put(XLT_PLATE_COLOR_YELLOW, PlateColor.yellow);
        mapXltPalteColorToEnum.put(XLT_PLATE_COLOR_BLACK, PlateColor.black);
        mapXltPalteColorToEnum.put(XLT_PLATE_COLOR_WHITE, PlateColor.white);
        mapXltPalteColorToEnum.put(XLT_PLATE_COLOR_GREEN, PlateColor.green);
        
        //停车异常信息映射表
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_ZC, ParkAbnormal.zc);
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_KW, ParkAbnormal.kw);
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_XW, ParkAbnormal.xw);
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_YX, ParkAbnormal.yx);
    } 
    
    @Override
    public Event convert2Event(XltEvt xltEvt) throws BusinessException
    {
        Event retEvent = new Event();
        //设备厂商：信路通
        retEvent.setDeviceCompany(DEVICE_COMPANY_XLT);
        
        //事件类型
        retEvent.setType(mapXltEventTypeToEnum.get(xltEvt.getEvt()));
        
        //属性赋值
        retEvent.setGuid(xltEvt.getEvtGuid());
        retEvent.setActId(xltEvt.getParkingActId());
        retEvent.setHappenTime(xltEvt.getHappenTime());
        retEvent.setPlateNo(xltEvt.getPlateNumber().trim().toUpperCase());
        retEvent.setPlateColor(mapXltPalteColorToEnum.get(xltEvt.getPlateColor()));
        //出/入事件的图片
        if (null != xltEvt.getPicUrlArr())
        {
            setupPics(xltEvt.getPicUrlArr(), xltEvt.getEvt(), retEvent);
        }
        
        //停车完成事件图片
        if (null != xltEvt.getPicUrlArrIn())
        {
            setupPics(xltEvt.getPicUrlArrIn(), XLT_EVENT_CAR_IN, retEvent);
        }
        if (null != xltEvt.getPicUrlArrOut())
        {
            setupPics(xltEvt.getPicUrlArrOut(), XLT_EVENT_CAR_OUT, retEvent);
        }
        
        retEvent.setParkCode(xltEvt.getParkingCode());
        retEvent.setParkName(xltEvt.getParkingName());
        retEvent.setPlaceCode(xltEvt.getBerthCode());
        retEvent.setDeviceSn(xltEvt.getDeviceSn());
        retEvent.setDevicePlace(xltEvt.getDevicePlace());
        retEvent.setPlateCredible(xltEvt.getPlateCredible());
        retEvent.setActionCredible(xltEvt.getActionCredible());
        
        //异常停车类型
        retEvent.setParkingAbnormal(mapXltParkAbnormalToEnum.get(xltEvt.getParkingAbnormal()));
        retEvent.setGeo(xltEvt.getGeo());
        
        //出入场信息
        retEvent.setTimeIn(xltEvt.getTimeIn());
        retEvent.setPicUrlIn(xltEvt.getPicUrlIn());
        retEvent.setTimeOut(xltEvt.getTimeOut());
        retEvent.setPicUrlOut(xltEvt.getPicUrlOut());
        
        //取消事件
        retEvent.setTargetGuid(xltEvt.getTargetEvtGuid());
        retEvent.setTargetType(mapXltEventTypeToEnum.get(xltEvt.getTargetEvtType()));
        
        //有效
        retEvent.setEnabled("Y");
        
        return retEvent;
    }

    @Override
    public XltEvtResult convert2EventResp(EventResult eventResult)
    {
        XltEvtResult xltEvtResult = new XltEvtResult();
        String msg = String.format("%s,%s,%s,%s", 
                eventResult.getContent().getLine1(),
                eventResult.getContent().getLine2(),
                eventResult.getContent().getLine3(),
                eventResult.getContent().getLine4());
        xltEvtResult.setMessage(msg);
        if (eventResult.getOpen())
        {
            xltEvtResult.setErrorcode(CLT_RET_CODE_OK);
        }
        else
        {
            xltEvtResult.setErrorcode(CLT_RET_CODE_FAILED);
        }
        return xltEvtResult;
    }
    
    /**
     * 设置停车图片
     * @param arrayList 接口传递的图片url数组
     * @param picType 图片类型
     * @param event 目标事件
     */
    private void setupPics(ArrayList<String> arrayList, String picType, Event event) 
    {
        Set<EventPic> eventPics = new HashSet<>();
        
        for (String picUrl : arrayList)
        {
            EventPic eventPic = new EventPic();
            eventPic.setPicUrl(picUrl);
            eventPic.setPicType(picType);
            eventPic.setEvent(event);
            eventPics.add(eventPic);
        }
        event.setPics(eventPics);
        return;
    }

    @Override
    public XltEvtResult failedResp(String messge)
    {
        XltEvtResult resp = new XltEvtResult();
        resp.setErrorcode(CLT_RET_CODE_FAILED);
        resp.setMessage(messge);
        return resp;
    }
}
