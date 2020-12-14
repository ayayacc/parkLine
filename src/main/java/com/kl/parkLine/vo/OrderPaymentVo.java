package com.kl.parkLine.vo;

import java.math.BigDecimal;
import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(name="orderId")
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
    @JSONField(name="parkId")
    private Integer orderParkParkId;
    
    /**
     * 停车场名称
     */
    @ApiModelProperty(name="停车场名称")
    @JSONField(name="parkName")
    private String orderParkName;
    
    /**
     * 车辆Id
     */
    @ApiModelProperty(name="车辆Id", value="carId")
    @JSONField(name="carId")
    private Integer orderCarCarId;
    
    /**
     * 车牌号码
     */
    @ApiModelProperty(name="车牌号码", value="carNo")
    @JSONField(name="carNo")
    private String orderCarCarNo;
    
    @ApiModelProperty(name="车辆入场时间", value="inTime")
    @JSONField(format="yyyy-MM-dd HH:mm:ss", name="inTime")
    private Date orderInTime; 
    
    @ApiModelProperty(name="车辆出场时间", value="outTime")
    @JSONField(format="yyyy-MM-dd HH:mm:ss",name="outTime")
    private Date orderOutTime; 
    
    @ApiModelProperty(name="金额(元)", value="amt")
    private BigDecimal orderAmt; 
    
    @ApiModelProperty(name="使用优惠券后实付金额(元)", value="amt")
    @JSONField(name="payedAmt")
    private BigDecimal amt; 
    
    @ApiModelProperty(name="钱包余额(元)", value="walletBalance")
    private BigDecimal walletBalance; 
    
    @ApiModelProperty(name="付款时间", value="paymentTime")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    private Date paymentTime; 
    
    @JSONField(format="yyyy-MM-dd",name="startDate")
    @ApiModelProperty(name="月票开始时间", value="startDate")
    private Date orderStartDate; 
    
    /**
     * 结束时间
     */
    @JSONField(format="yyyy-MM-dd",name="endDate")
    @ApiModelProperty(name="结束时间", value="startDate")
    private Date orderEndDate; 
    
    /**
     * 入场截图url
     */
    @JSONField(name="inImgCode")
    private String orderInImgCode;
    
    /**
     * 出场截图url
     */
    @JSONField(name="outImgCode")
    private String orderOutImgCode;
}
