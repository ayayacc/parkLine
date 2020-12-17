package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
@Getter
@Setter
@ToString
public class BoyueResp
{
    /**
     * "info":"ok",//回复ok 开闸, 其他不开闸
     */
    private String info;
    
    /**
     * 当前车牌id
     */
    @JSONField(name="plateid")
    private Integer plateId;
    
    /**
     * 显示屏内容
     */
    private String content;
}
