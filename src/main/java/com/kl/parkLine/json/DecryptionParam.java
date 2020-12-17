package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("解密参数")
public class DecryptionParam
{
    //加密数据
    private String encryptedData;
    //加密算法的初始向量
    private String iv;
}
