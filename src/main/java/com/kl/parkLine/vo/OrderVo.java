package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("订单VO")
public class OrderVo
{
    @ApiModelProperty(name="订单Id", position=0)
    private Integer orderId;
    
    @ApiModelProperty(name="订单编码", position=1)
    private String code;
    
    @ApiModelProperty(name="订单类型", position=2)
    private OrderType type;
    
    @ApiModelProperty(name="订单状态", position=3)
    private OrderStatus status;
    
    /**
     * 停车场Id
     */
    @ApiModelProperty(name="停车场Id", value="parkId")
    @JsonProperty(value = "parkId")
    private Integer parkParkId;
    
    /**
     * 停车场名称
     */
    @ApiModelProperty(name="停车场名称")
    private String parkName;
    
    /**
     * 车辆Id
     */
    @ApiModelProperty(name="车辆Id", value="carId")
    @JsonProperty(value = "carId")
    private Integer carCarId;
    
    /**
     * 车牌号码
     */
    @ApiModelProperty(name="车牌号码", value="carNo")
    @JsonProperty(value = "carNo")
    private String carCarNo;
    
    @ApiModelProperty(name="车辆入场时间", value="inTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date inTime; 
    
    @ApiModelProperty(name="车辆出场时间", value="outTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date outTime; 
    
    @ApiModelProperty(name="金额(元)", value="amt")
    private BigDecimal amt; 
    
    @ApiModelProperty(name="使用优惠券后实付金额(元)", value="amt")
    private BigDecimal realAmt; 
    
    @ApiModelProperty(name="钱包余额(元)", value="walletBalance")
    private BigDecimal walletBalance; 
    
    @ApiModelProperty(name="付款时间", value="paymentTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime; 
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(name="月票开始时间", value="startDate")
    private Date startDate; 
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(name="结束时间", value="startDate")
    private Date endDate; 
    
    /**
     * 入场截图url
     */
    private String inImgCode;
    
    /**
     * 出场截图url
     */
    private String outImgCode;
}
