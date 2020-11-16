package com.kl.parkLine.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("月票参数")
public class MonthlyTktParam
{
    @ApiModelProperty(required = true, name = "停车场Id")
    private Integer parkId;
    
    @ApiModelProperty(required = true, name = "车辆Id")
    private Integer carId;
    
    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true, name = "有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(required = true, name = "有效期结束时间")
    private Date endDate;
}
