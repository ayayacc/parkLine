package com.kl.parkLine.boyue;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class AlarmInfoPlate
{
    //设备序列号，设备唯一
    private String serialno;
    
    //设备名称
    private String deviceName;
    
    //设备ip地址
    private String ipaddr;
    
    //识别结果
    private BoyueResult result;
    
    //识别结果对应帧的时间戳
    private TimeStamp timeStamp;
    
    //识别结果车牌ID
    @JsonProperty("plateid")
    private Integer plateId;
    
}
