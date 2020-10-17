package com.kl.parkLine.vo;

import java.util.Set;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
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
