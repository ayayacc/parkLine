package com.kl.parkLine.json;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QqMapSearchResult extends WxResultBase
{
    private Integer status;  
    private String message;  
    private Integer count;  
    private List<QqMapPoiItem> data;
}
