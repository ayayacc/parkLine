package com.kl.parkLine.json;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("购买月票参数")
public class MonthlyTktParam
{
    @ApiModelProperty("停车场Id")
    private Integer parkId;
    
    @ApiModelProperty(name="车牌号码")
    private String carNo;
    
    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(name="有效期开始时间")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty(name="有效期结束时间")
    private Date endDate;
    
    @ApiModelProperty("金额(元)")
    private BigDecimal amt;
}