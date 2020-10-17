package com.kl.parkLine.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 月票
 *
 * <p>月票
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@Builder
@ApiModel("月票Vo")
public class MonthlyTktVo
{
    @ApiModelProperty(name="月票Id")
    private Integer monthlyTicketId;
    
    /**
     * 月票编号
     */
    @ApiModelProperty(name="月票code")
    private String code;
    
    /**
     * 停车场Id
     */
    @ApiModelProperty(name="停车场Id")
    private Integer parkId;
    
    /**
     * 停车场名称
     */
    @ApiModelProperty(name="停车场名称")
    private String parkName;
    
    /**
     * 车辆Id
     */
    @ApiModelProperty(name="车辆Id")
    private Integer carId;
    
    /**
     * 车牌号码
     */
    @ApiModelProperty(name="车牌号码")
    private String carNo;
    
    /**
     * 是否有效
     */
    @ApiModelProperty(name="状态")
    private String status;
    
    /**
     * 有效期开始时间
     */
    @ApiModelProperty(name="有效期开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;
    
    /**
     * 有效期结束时间
     */
    @ApiModelProperty(name="有效期结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;
}
