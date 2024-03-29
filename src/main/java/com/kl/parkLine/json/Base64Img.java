package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("Base64图片信息")
public class Base64Img
{
    @ApiModelProperty("base64字符串")
    private String img;
}
