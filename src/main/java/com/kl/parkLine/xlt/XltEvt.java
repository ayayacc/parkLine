package com.kl.parkLine.xlt;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class XltEvt
{
    //事件类型:evt.car.in/evt.car.out/evt.car.complete
    private String evt;
    
    //事件唯一标识符
    private String evtGuid;
    
    //停车行为id，同一次进出完成，将拥有同一个停车行为id
    private String parkingActId;
    
    //事件发生时间 yyyy-MM-dd HH:mm:ss
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date happenTime;
    
    //车牌号码
    private String plateNumber;
    
    //车牌颜色:未知:0,蓝:1,黄:2, 黑:3,白:4, 绿:5
    private Integer plateColor; 
    
    /**
     * 鉴定图片集
     * 顺序说明： 1、车牌图,2、抓拍图1,3、抓拍图2,4、车身图(非必然存在)
     */
    private ArrayList<String> picUrlArr;
    
    //停车点编码
    private String parkingCode;
    
    //停车点名称
    private String parkingName;
    
    //泊位号
    private String berthCode;
    
    //设备编号
    private String deviceSn;
    
    //设备安装详细地址
    private String devicePlace;
    
    //车牌识别可信度
    private BigDecimal plateCredible;
    
    //出入行为可信度
    private BigDecimal actionCredible;
    
    //异常停车类型
    private Integer parkingAbnormal;
    
    //设备经纬度，不为空时，格式如下： “108.23 22.45”
    private String geo;
    
    //入场时间
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date timeIn;
    
    //入场鉴定图片，地址为图片绝对地址，如：http://10.7.7.15:8880/group1.jpg
    private String picUrlIn;
    
    //出场时间
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date timeOut;
    
    //出场鉴定图片，地址为图片绝对地址，如：http://10.7.7.15:8880/group1.jpg
    private String picUrlOut;
    
    /**
     * 鉴定图片集
     * 顺序说明： 1、车牌图,2、抓拍图1,3、抓拍图2,4、车身图(非必然存在)
     */
    private ArrayList<String> picUrlArrIn;
    
    /**
     * 鉴定图片集
     * 顺序说明： 1、车牌图,2、抓拍图1,3、抓拍图2,4、车身图(非必然存在)
     */
    private ArrayList<String> picUrlArrOut;
    
    /**
     * 作废目标事件的guid
     */
    private String targetEvtGuid;
    
    /**
     * 事件类型，此处为作废目标事件类型
     */
    private String targetEvtType;
    
}
