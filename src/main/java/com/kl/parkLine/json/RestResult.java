package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@ApiModel("api通用返回数据")
public class RestResult<T>
{
    @ApiModelProperty("标识代码,0表示成功，非0表示出错")
    private Integer retCode;  
    
    @ApiModelProperty("提示信息,供报错时使用")
    private String errMsg;  
    
    @ApiModelProperty("返回的数据")
    private T data;
}
