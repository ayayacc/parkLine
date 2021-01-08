package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QqMapPoiItem
{
    private String id;  
    private String title;  
    private String address;  
    private String tel;  
    private String category;  
    private Integer type;  
    private QqMapLocation location;  
    @JSONField(name="ad_info")
    private QqMapAddInfo addInfo;
}
