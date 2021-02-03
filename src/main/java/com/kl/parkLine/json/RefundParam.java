package com.kl.parkLine.json;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("退款参数")
public class RefundParam
{
    @ApiModelProperty("用户Id")
    private Integer userId;
    
    @ApiModelProperty("退款金额")
    private BigDecimal amt;
    
    @ApiModelProperty("退款人")
    private String refundBy;
    
    @ApiModelProperty("退款时间")
    private Long refundTime;
    
    @ApiModelProperty("备注")
    private String remark;
    
    @ApiModelProperty("公钥")
    private String publicKey;
    
    @ApiModelProperty("签名,md5(userId=xxx&amt=xxx(补齐2位小数)&refundBy=xxx&refundTime=xxx(timestamp)&remark=xxx&publicKey=xxx&privateKey=xxx)")
    private String sign;
}
