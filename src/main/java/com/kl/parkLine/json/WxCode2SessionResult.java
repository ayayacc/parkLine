package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WxCode2SessionResult extends WxResultBase
{
    private String openid;  
    @JSONField(name="session_key")
    private String sessionKey;  
    private String unionid;  
}
