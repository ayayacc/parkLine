package com.kl.parkLine.boyue;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class AlarmInfoPlate
{
    //设备序列号，设备唯一
    private String serialno;
    
    //识别结果
    private BoyueResult result;
}
