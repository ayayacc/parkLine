package com.kl.parkLine.boyue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoyueResp
{
    /**
     * "info":"ok",//回复ok 开闸, 其他不开闸
     */
    private String info;
    
    /**
     * 当前车牌id
     */
    @JsonProperty("plateid")
    private Integer plateId;
    
    /**
     * 显示屏内容
     */
    private String content;
    
    /**
     * 是否付款
     */
    @JsonProperty("is_pay")
    private Boolean isPay;
}
