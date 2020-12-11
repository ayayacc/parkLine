package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date inTime; 
    
    @ApiModelProperty(name="车辆出场时间", value="outTime")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date outTime; 
    
    @ApiModelProperty(name="车辆出场限制时间", value="outTimeLimit")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date outTimeLimit; 
    
    @ApiModelProperty(name="金使用优惠券前订单金额(元)", value="amt")
    private BigDecimal amt; 
    
    @ApiModelProperty(name="使用优惠券前已付款金额(元)", value="payedAmt")
    private BigDecimal payedAmt; 
    
    @ApiModelProperty(name="使用优惠券后未付金额(元)", value="realUnpayedAmt")
    private BigDecimal realUnpayedAmt; 
    
    @ApiModelProperty(name="使用优惠券后实际已付金额(元)", value="realPayedAmt")
    private BigDecimal realPayedAmt; 
    
    @ApiModelProperty(name="最后付款时间", value="lastPaymentTime")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @JsonProperty(value = "paymentTime")
    private Date lastPaymentTime; 
    
    @JSONField(format="yyyy-MM-dd")
    @ApiModelProperty(name="月票开始时间", value="startDate")
    private Date startDate; 
    
    /**
     * 结束时间
     */
    @JSONField(format="yyyy-MM-dd")
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
