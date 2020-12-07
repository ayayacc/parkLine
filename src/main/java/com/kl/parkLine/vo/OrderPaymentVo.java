package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@ApiModel("订单付款记录VO")
public class OrderPaymentVo
{
    @ApiModelProperty(name="付款Id")
    private Integer orderPaymentId;
    
    @ApiModelProperty(name="订单Id")
    @JsonProperty(value = "orderId")
    private Integer orderOrderId;
    
    @ApiModelProperty(name="订单编码")
    private String orderCode;
    
    @ApiModelProperty(name="订单类型")
    private OrderType orderType;
    
    @ApiModelProperty(name="订单状态")
    private OrderStatus orderStatus;
    
    /**
     * 停车场Id
     */
    @ApiModelProperty(name="停车场Id", value="parkId")
    @JsonProperty(value = "parkId")
    private Integer orderParkParkId;
    
    /**
     * 停车场名称
     */
    @ApiModelProperty(name="停车场名称")
    @JsonProperty(value = "parkName")
    private String orderParkName;
    
    /**
     * 车辆Id
     */
    @ApiModelProperty(name="车辆Id", value="carId")
    @JsonProperty(value = "carId")
    private Integer orderCarCarId;
    
    /**
     * 车牌号码
     */
    @ApiModelProperty(name="车牌号码", value="carNo")
    @JsonProperty(value = "carNo")
    private String orderCarCarNo;
    
    @ApiModelProperty(name="车辆入场时间", value="inTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty(value = "inTime")
    private Date orderInTime; 
    
    @ApiModelProperty(name="车辆出场时间", value="outTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @JsonProperty(value = "outTime")
    private Date orderOutTime; 
    
    @ApiModelProperty(name="金额(元)", value="amt")
    private BigDecimal orderAmt; 
    
    @ApiModelProperty(name="使用优惠券后实付金额(元)", value="amt")
    @JsonProperty(value = "payedAmt")
    private BigDecimal amt; 
    
    @ApiModelProperty(name="钱包余额(元)", value="walletBalance")
    private BigDecimal walletBalance; 
    
    @ApiModelProperty(name="付款时间", value="paymentTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date paymentTime; 
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(name="月票开始时间", value="startDate")
    @JsonProperty(value = "startDate")
    private Date orderStartDate; 
    
    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @ApiModelProperty(name="结束时间", value="startDate")
    @JsonProperty(value = "endDate")
    private Date orderEndDate; 
    
    /**
     * 入场截图url
     */
    @JsonProperty(value = "inImgCode")
    private String orderInImgCode;
    
    /**
     * 出场截图url
     */
    @JsonProperty(value = "outImgCode")
    private String orderOutImgCode;
}
