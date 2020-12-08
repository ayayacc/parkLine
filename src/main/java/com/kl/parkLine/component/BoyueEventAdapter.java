package com.kl.parkLine.component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.kl.parkLine.boyue.BoyueEvent;
import com.kl.parkLine.boyue.BoyueResp;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Event;
import com.kl.parkLine.enums.EventType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.interfaces.IEventAdapter;
import com.kl.parkLine.json.EventResult;
import com.kl.parkLine.service.DeviceService;

/**
 * @author chenc
 */
@Component
public class BoyueEventAdapter implements IEventAdapter<BoyueEvent, BoyueResp>
{
    private final static Logger logger = LoggerFactory.getLogger(BoyueEventAdapter.class);
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private AliYunCmpt aliYunCmpt;
    
    private final  String DEVICE_COMPANY_BY = "博粤";
    
    //车牌颜色, 0：未知、1：蓝色、2：黄色、3：白色、4：黑色、5：绿色
    private final Integer BY_PLATE_COLOR_UNKNOWN = 0;
    private final Integer BY_PLATE_COLOR_BLUE = 1;
    private final Integer BY_PLATE_COLOR_YELLOW = 2;
    private final Integer BY_PLATE_COLOR_WHITE = 3;
    private final Integer BY_PLATE_COLOR_BLACK = 4;
    private final Integer BY_PLATE_COLOR_GREEN = 5;
    //车牌颜色映射表
    private final Map<Integer, PlateColor> mapByPalteColorToEnum = new HashMap<Integer, PlateColor>();
    
    //博粤开闸
    private final String BOYUE_OPEN = "ok";
    private final String BOYUE_NOT_OPEN = "notok";
    
    public BoyueEventAdapter()
    {
        //博粤车牌颜色映射表
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_UNKNOWN, PlateColor.unknown);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_BLUE, PlateColor.blue);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_YELLOW, PlateColor.yellow);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_BLACK, PlateColor.black);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_WHITE, PlateColor.white);
        mapByPalteColorToEnum.put(BY_PLATE_COLOR_GREEN, PlateColor.green);
    } 
    
    @Override
    public Event convert2Event(BoyueEvent boyueEvt) throws BusinessException
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
        try
        {
            aliYunCmpt.upload(boyueEvt.getAlarmInfoPlate().getResult().getPlateResult().getImageFile(), code);
        }
        catch (IOException e)
        {
            logger.error(String.format("图片上传阿里云失败:%s", e.getMessage()));
            e.printStackTrace();
        }
        
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

    @Override
    public BoyueResp convert2EventResp(EventResult eventResult)
    {
        BoyueResp boyueResp = new BoyueResp();
        boyueResp.setContent(eventResult.getContent());
        if (eventResult.getOpen())
        {
            boyueResp.setInfo(BOYUE_OPEN);
        }
        else
        {
            boyueResp.setInfo(BOYUE_NOT_OPEN);
        }
        return boyueResp;
    }

    @Override
    public BoyueResp failedResp(String messge)
    {
        BoyueResp resp = new BoyueResp();
        resp.setInfo(BOYUE_OPEN); //出现异常回复ok开闸
        return null;
    }
}
