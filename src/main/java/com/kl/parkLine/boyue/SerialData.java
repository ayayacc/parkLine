package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
@Builder
public class SerialData
{
    /**
     * 常量0
     */
    @JSONField(name="serialChannel")
    private Integer serialChannel;
    
    /**
     * data 字节流的base64编码
     */
    @JSONField(name="data")
    private String data;
    
    /**
     * data 字节流长度
     */
    @JSONField(name="dataLen")
    private Integer dataLen;
    
    @JSONField(serialize=false)
    private String content;
}
