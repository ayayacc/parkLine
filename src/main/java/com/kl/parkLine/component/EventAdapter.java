package com.kl.parkLine.component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kl.parkLine.entity.Dict;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.EventPic;
import com.kl.parkLine.service.DictService;
import com.kl.parkLine.util.DictCode;
import com.kl.parkLine.xlt.XltEvt;

/**
 * @author chenc
 */
@Component
public class EventAdapter
{
    @Autowired
    private DictService dictService;
    
    private final  String DEVICE_COMPANY_XLT = "信路通";
    
    //事件类型
    private final String XLT_EVENT_CAR_IN = "evt.car.in";
    private final String XLT_EVENT_CAR_OUT = "evt.car.out";
    private final String XLT_EVENT_CAR_COMPLETE = "evt.car.complete";
    private final String XLT_EVENT_CANCEL = "evt.cancel";
    //事件映射表
    private final Map<String, String> mapXltEventTypeToDictCode = new HashMap<String, String>();
    
    //车牌颜色, 未知:0,蓝:1,黄:2, 黑:3,白:4, 绿:5
    private final Integer XLT_PLATE_COLOR_UNKNOWN = 0;
    private final Integer XLT_PLATE_COLOR_BLUE = 1;
    private final Integer XLT_PLATE_COLOR_YELLOW = 2;
    private final Integer XLT_PLATE_COLOR_BLACK = 3;
    private final Integer XLT_PLATE_COLOR_WHITE = 4;
    private final Integer XLT_PLATE_COLOR_GREEN = 5;
    //车牌颜色映射表
    private final Map<Integer, String> mapXltPalteColorToDictCode = new HashMap<Integer, String>();
    
    //停车异常信息
    private final Integer XLT_PARK_ABNORMAL_ZC = 0;
    private final Integer XLT_PARK_ABNORMAL_KW = 1;
    private final Integer XLT_PARK_ABNORMAL_XW = 2;
    private final Integer XLT_PARK_ABNORMAL_YX = 3;
    //停车异常映射表
    private final Map<Integer, String> mapXltParkAbnormalToDictCode = new HashMap<Integer, String>();

    public EventAdapter()
    {
        //事件映射表
        mapXltEventTypeToDictCode.put(XLT_EVENT_CAR_IN, DictCode.EVENT_TYPE_CAR_IN);
        mapXltEventTypeToDictCode.put(XLT_EVENT_CAR_OUT, DictCode.EVENT_TYPE_CAR_OUT);
        mapXltEventTypeToDictCode.put(XLT_EVENT_CAR_COMPLETE, DictCode.EVENT_TYPE_CAR_COMPLETE);
        mapXltEventTypeToDictCode.put(XLT_EVENT_CANCEL, DictCode.EVENT_TYPE_CANCEL);
        
        //车牌颜色映射表
        mapXltPalteColorToDictCode.put(XLT_PLATE_COLOR_UNKNOWN, DictCode.PLATE_COLOR_UNKNOWN);
        mapXltPalteColorToDictCode.put(XLT_PLATE_COLOR_BLUE, DictCode.PLATE_COLOR_BLUE);
        mapXltPalteColorToDictCode.put(XLT_PLATE_COLOR_YELLOW, DictCode.PLATE_COLOR_YELLOW);
        mapXltPalteColorToDictCode.put(XLT_PLATE_COLOR_BLACK, DictCode.PLATE_COLOR_BLACK);
        mapXltPalteColorToDictCode.put(XLT_PLATE_COLOR_WHITE, DictCode.PLATE_COLOR_WHITE);
        mapXltPalteColorToDictCode.put(XLT_PLATE_COLOR_GREEN, DictCode.PLATE_COLOR_GREEN);
        
        //停车异常信息映射表
        mapXltParkAbnormalToDictCode.put(XLT_PARK_ABNORMAL_ZC, DictCode.PARK_ABNORMAL_ZC);
        mapXltParkAbnormalToDictCode.put(XLT_PARK_ABNORMAL_KW, DictCode.PARK_ABNORMAL_KW);
        mapXltParkAbnormalToDictCode.put(XLT_PARK_ABNORMAL_XW, DictCode.PARK_ABNORMAL_XW);
        mapXltParkAbnormalToDictCode.put(XLT_PARK_ABNORMAL_YX, DictCode.PARK_ABNORMAL_YX);
    } 
    
    /**
     * 将信路通evt转换成标准Event
     * @param xltEvt
     * @return
     */
    public Event xlt2Event(XltEvt xltEvt)
    {
        Event retEvent = new Event();
        //设备厂商：信路通
        retEvent.setDeviceCompany(DEVICE_COMPANY_XLT);
        
        //事件类型
        String dictCode = mapXltEventTypeToDictCode.get(xltEvt.getEvt());
        Dict dict = dictService.findOneByCode(dictCode);
        retEvent.setType(dict);
        
        //属性赋值
        retEvent.setGuid(xltEvt.getEvtGuid());
        retEvent.setActId(xltEvt.getParkingActId());
        retEvent.setHappenTime(xltEvt.getHappenTime());
        retEvent.setPlateNo(xltEvt.getPlateNumber());
        dictCode = mapXltPalteColorToDictCode.get(xltEvt.getPlateColor());
        dict = dictService.findOneByCode(dictCode);
        retEvent.setPlateColor(dict);
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
        dictCode = mapXltParkAbnormalToDictCode.get(xltEvt.getParkingAbnormal());
        dict = dictService.findOneByCode(dictCode);
        retEvent.setParkingAbnormal(dict);
        retEvent.setGeo(xltEvt.getGeo());
        
        //出入场信息
        retEvent.setTimeIn(xltEvt.getTimeIn());
        retEvent.setPicUrlIn(xltEvt.getPicUrlIn());
        retEvent.setTimeOut(xltEvt.getTimeOut());
        retEvent.setPicUrlOut(xltEvt.getPicUrlOut());
        
        //取消事件
        retEvent.setTargetGuid(xltEvt.getTargetEvtGuid());
        dictCode = mapXltEventTypeToDictCode.get(xltEvt.getTargetEvtType());
        dict = dictService.findOneByCode(dictCode);
        retEvent.setTargetType(dict);
        
        //有效
        retEvent.setEnabled("Y");
        
        return retEvent;
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
    
}
