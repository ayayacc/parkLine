package com.kl.parkLine.json;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.enums.PlaceType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("月票参数")
public class MonthlyTktParam
{
    @ApiModelProperty(required = true, name = "停车场Id")
    private Integer parkId;
    
    @ApiModelProperty(required = true, name = "车辆Id")
    private Integer carId;
    
    @ApiModelProperty(required = true, name = "车位类型:ground(地面)/underground(地下)")
    private PlaceType placeType;
    
    /**
     * 有效期开始时间
     */
    @JSONField(format="yyyy-MM-dd")
    @ApiModelProperty(required = true, name = "有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @JSONField(format="yyyy-MM-dd")
    @ApiModelProperty(required = true, name = "有效期结束时间")
    private Date endDate;
}
