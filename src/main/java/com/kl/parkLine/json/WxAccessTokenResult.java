package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WxAccessTokenResult extends WxResultBase
{
    @JSONField(name="access_token")
    private String accessToken;  
    
    @JSONField(name="expires_in")
    private Integer expiresIn;  
}
