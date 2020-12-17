package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@ToString
@ApiModel("角色Vo")
public class RoleVo
{
    @ApiModelProperty("停车场Id")
    private Integer roleId;
    
    @ApiModelProperty("编码")
    private String code;
    
    @ApiModelProperty("名称")
    private String name;
}
