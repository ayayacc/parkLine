package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BoyueRespWrap
{
    @JSONField(name="Response_AlarmInfoPlate")
    private BoyueResp boyueResp;
}
