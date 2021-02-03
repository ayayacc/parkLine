package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("解密结果")
public class DecryptionUserResult
{
    private String openId;
    private String unionId;
}
