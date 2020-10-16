package com.kl.parkLine.vo;

import java.math.BigDecimal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("发票VO")
public class InvoiceVo
{
    @ApiModelProperty("发票Id")
    private Integer invoiceId;
    
    /**
     * 开票编号
     */
    @ApiModelProperty("发票编码")
    private String code;
    
    /**
     * 开票金额
     */
    @ApiModelProperty("发票金额")
    private BigDecimal amt;
    
    /**
     * 状态
     */
    @ApiModelProperty("状态")
    private String status;
}
