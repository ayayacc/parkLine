package com.kl.parkLine.vo;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kl.parkLine.enums.Gender;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("用户VO")
public class UserVo
{
    @ApiModelProperty("用户Id")
    private Integer userId;
    
    @ApiModelProperty("用户编号，系统自动生成，唯一")
    private String name;
    
    @ApiModelProperty("用户昵称")
    private String nickName;
    
    @ApiModelProperty("手机号码")
    private String mobile;
    
    @ApiModelProperty("性别")
    private Gender gender;
    
    @ApiModelProperty("钱包余额")
    private BigDecimal walletBalance;
    
    @ApiModelProperty("绑定的车辆数量")
    private Integer carCnt;
    
    @ApiModelProperty("未支付的订单数量")
    private Integer needToPayCnt;
    
    @ApiModelProperty("是否有效")
    private boolean isEnable;
}
