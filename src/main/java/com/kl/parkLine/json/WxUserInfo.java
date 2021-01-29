package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WxUserInfo extends WxResultBase
{
    @JSONField(name="unionid")
    private String unionId;
    private String wxXcxOpenId;
    @JSONField(name="openid")
    private String wxGzhOpenId;
    private String sessionKey;
    @JSONField(name="nickname")
    private String nickName;
    @JSONField(name="country")
    private String country;
    @JSONField(name="province")
    private String province;
    @JSONField(name="city")
    private String city;
    @JSONField(name="gender")
    private Integer gender;
    @JSONField(name="sex")
    private Integer sex;
    private String subscribe;
}
