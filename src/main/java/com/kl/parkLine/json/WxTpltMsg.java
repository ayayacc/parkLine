package com.kl.parkLine.json;

import java.util.Map;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
public class WxTpltMsg
{
    @JSONField(name="touser")
    private String toUser;  
    
    @JSONField(name="template_id")
    private String templateId;  
    
    @JSONField(name="url")
    private String url;  
    
    @JSONField(name="topcolor")
    private String topColor;
    
    private Map<String, WxTpltMsgData> data; //data数据
}
