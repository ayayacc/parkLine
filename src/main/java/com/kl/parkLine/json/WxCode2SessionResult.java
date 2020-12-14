package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WxCode2SessionResult extends WxResultBase
{
    private String openid;  
    @JSONField(name="session_key")
    private String sessionKey;  
    private String unionid;  
}
