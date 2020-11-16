package com.kl.parkLine.json;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("微信uniorder成功参数")
public class WxUnifiedOrderResult
{
    @ApiModelProperty("小程序ID")
    private String appId;
    
    @ApiModelProperty("数据包")
    @JsonProperty(value = "package")
    private String pack;
    
    @ApiModelProperty("时间戳")
    private String timeStamp;
    
    @ApiModelProperty("随机串")
    private String nonceStr;
    
    @ApiModelProperty("签名方式")
    private String signType;
    
    @ApiModelProperty("签名")
    private String paySign ;
    
    @ApiModelProperty("订单编号")
    private String orderCode ;
    
}