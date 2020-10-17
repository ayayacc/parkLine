package com.kl.parkLine.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("菜单Vo")
public class MenuVo
{
    @ApiModelProperty("菜单Id")
    private Integer menuId;

    @ApiModelProperty("菜单名称")
    private String name;

    @ApiModelProperty("菜单url")
    private String url;
    
    @ApiModelProperty("菜单排序")
    private String sortIdx;
    
    @ApiModelProperty("父菜单Id")
    private Integer parentId; 
}
