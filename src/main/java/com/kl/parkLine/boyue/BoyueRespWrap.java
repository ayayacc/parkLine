package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class BoyueRespWrap
{
    @JSONField(name="Response_AlarmInfoPlate")
    private BoyueResp boyueResp;
}
