package com.kl.parkLine.enums;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel("角色类型")
public enum RoleType implements BaseEnum
{
    @ApiModelProperty("1:公司")
    company(1, "公司"),
    
    @ApiModelProperty("2:停车场")
    park(2, "停车场"),
    
    @ApiModelProperty("3:终端用户")
    endUser(3, "终端用户");
    
    private Integer value;
    private String text;

}
