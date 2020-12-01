package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QqMapPoiItem extends WxResultBase
{
    private String id;  
    private String title;  
    private String address;  
    private String tel;  
    private String category;  
    private Integer type;  
    private QqMapLocation location;  
}
