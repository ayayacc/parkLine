package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WxLoginParam
{
    private String code;
    private WxUserInfo userInfo;
}
