package com.kl.parkLine.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderVo
{
    private Integer orderId;
    private String code;
    private String type;
    private String status;
}
