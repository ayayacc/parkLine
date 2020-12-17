package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WxUserInfo
{
    private String nickName;
    private String country;
    private String province;
    private String city;
    private Integer gender;
}
