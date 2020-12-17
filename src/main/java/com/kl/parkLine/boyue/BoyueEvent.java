package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class BoyueEvent
{
    @JSONField(name="AlarmInfoPlate")
    AlarmInfoPlate alarmInfoPlate;
}
