package com.kl.parkLine.vo;

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
