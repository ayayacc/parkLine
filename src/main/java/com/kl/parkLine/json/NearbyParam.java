package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("附近的xx查询条件")
public class NearbyParam
{
    @ApiModelProperty(required = true, name="中心点经纬度，如POINT(108.281 22.9033)")
    private String centerPoint;
    
    @ApiModelProperty(required = true, name="距离(公里)")
    private Double distanceKm;
}
