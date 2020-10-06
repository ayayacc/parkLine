package com.kl.parkLine.vo;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ParkVo
{
    private Integer parkId;
    private String code;
    private String name;
    private Integer totalCnt;
    private Integer availableCnt;
    private String geo;
    private String contact;
    private String enabled;
    private Integer freeTime;
    private Integer timeLev1;
    private BigDecimal priceLev1;
    private Integer timeLev2;
    private BigDecimal priceLev2;
    private BigDecimal maxAmt;
}
