package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeStamp
{
    @JSONField(name="Timeval")
    Timeval timeval;
}
