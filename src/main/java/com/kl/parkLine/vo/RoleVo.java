package com.kl.parkLine.vo;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("角色Vo")
public class RoleVo
{
    @ApiModelProperty("停车场Id")
    private Integer roleId;
    
    @ApiModelProperty("编码")
    private String code;
    
    @ApiModelProperty("名称")
    private String name;
    
    @ApiModelProperty("菜单")
    private Set<MenuVo> menuVos;
}
