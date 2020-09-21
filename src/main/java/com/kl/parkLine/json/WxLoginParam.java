package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WxLoginParam
{
    private String code;
    private WxUserInfo userInfo;
}
