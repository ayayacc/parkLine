package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class WxTpltMsgData
{
    @JSONField(name="value")
    private String value;  
    
    @JSONField(name="color")
    private String color;  
}
