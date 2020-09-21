package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WxCode2SessionResult extends WxResultBase
{
    private String openid;  
    private String session_key;  
    private String unionid;  
}
