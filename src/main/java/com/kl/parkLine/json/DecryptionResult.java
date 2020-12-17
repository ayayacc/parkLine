package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("解密结果")
public class DecryptionResult
{
    //用户绑定的手机号（国外手机号会有区号）
    private String phoneNumber;
    //没有区号的手机号
    private String purePhoneNumber;
    //国家编号
    private String countryCode;
    
    
}
