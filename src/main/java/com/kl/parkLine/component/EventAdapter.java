package com.kl.parkLine.component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.entity.EventPic;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.ParkAbnormal;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.service.DeviceService;
import com.kl.parkLine.xlt.XltEvt;

/**
 * @author chenc
 */
@Component
public class EventAdapter
{
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private AliYunOssCmpt aliYunOssCmpt;
    
    private final  String DEVICE_COMPANY_XLT = "信路通";
    private final  String DEVICE_COMPANY_BY = "博粤";
    
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
    
    
    //车牌颜色, 0：未知、1：蓝色、2：黄色、3：白色、4：黑色、5：绿色
    private final Integer BY_PLATE_COLOR_UNKNOWN = 0;
    private final Integer BY_PLATE_COLOR_BLUE = 1;
    private final Integer BY_PLATE_COLOR_YELLOW = 2;
    private final Integer BY_PLATE_COLOR_WHITE = 3;
    private final Integer BY_PLATE_COLOR_BLACK = 4;
    private final Integer BY_PLATE_COLOR_GREEN = 5;
    //车牌颜色映射表
    private final Map<Integer, PlateColor> mapByPalteColorToEnum = new HashMap<Integer, PlateColor>();
    
    //停车异常信息
    private final Integer XLT_PARK_ABNORMAL_ZC = 0;
    private final Integer XLT_PARK_ABNORMAL_KW = 1;
    private final Integer XLT_PARK_ABNORMAL_XW = 2;
    private final Integer XLT_PARK_ABNORMAL_YX = 3;
    //停车异常映射表
    private final Map<Integer, ParkAbnormal> mapXltParkAbnormalToEnum = new HashMap<Integer, ParkAbnormal>();

    public EventAdapter()
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
        
        //博粤车牌颜色映射表
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_UNKNOWN, PlateColor.unknown);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_BLUE, PlateColor.blue);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_YELLOW, PlateColor.yellow);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_BLACK, PlateColor.black);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_WHITE, PlateColor.white);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_GREEN, PlateColor.green);
        
        //停车异常信息映射表
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_ZC, ParkAbnormal.zc);
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_KW, ParkAbnormal.kw);
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_XW, ParkAbnormal.xw);
        mapXltParkAbnormalToEnum.put(XLT_PARK_ABNORMAL_YX, ParkAbnormal.yx);
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
    
    
    /**
     * 将博粤通知转换成标准Event
     * @param boyueEvt
     * @return
     * @throws BusinessException 
     * @throws IOException 
     */
    public Event boyue2Event(BoyueEvent boyueEvt) throws BusinessException, IOException
    {
        Event retEvent = new Event();
        //厂商
        retEvent.setDeviceCompany(DEVICE_COMPANY_BY);
        //设备序列号
        String serialNo = boyueEvt.getAlarmInfoPlate().getSerialno();
        Device device = deviceService.findOneBySerialNo(serialNo);
        if (null == device)
        {
            throw new BusinessException(String.format("无效的设备序列号: %s", serialNo));
        }
        retEvent.setDeviceSn(serialNo);

        //发生时间
        Long timeStamp = boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getTimeStamp().getTimeval().getSec();
        Date happenTime = new Date(timeStamp);
        retEvent.setHappenTime(happenTime);

        //车牌号
        String carNo = boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getPlateNo().trim().toUpperCase();
        
        //bse64图片处理
        //code: carNo-in/out-timeStamp.jpg
        Date now = new Date();
        String code = String.format("%s-%s-%d.jpg", carNo, device.getUseage(), now.getTime());
        aliYunOssCmpt.upload(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getImageFile(), code);
        
        //车牌识别Id 
        retEvent.setPlateId(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getPlateId());
        
        //事件类型
        switch (device.getUseage())
        {
            case in:
                retEvent.setTimeIn(happenTime);
                retEvent.setType(EventType.in);
                retEvent.setPicUrlIn(code);  
                break;
            case out:
                retEvent.setTimeOut(happenTime);
                retEvent.setType(EventType.complete); //道闸停车将出场当作完成处理
                retEvent.setPicUrlOut(code);  
            default:
                break;
        }
        
        //车牌号码
        retEvent.setPlateNo(carNo);
        
        //车牌颜色
        retEvent.setPlateColor(mapByPalteColorToEnum.get(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getColorType()));
        
        //车牌可信度
        retEvent.setPlateCredible(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getConfidence().divide(new BigDecimal(100), 2, RoundingMode.HALF_UP));
        
        //是否有效
        retEvent.setEnabled("Y");
        return retEvent;
    }
}
