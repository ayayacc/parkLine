package com.kl.parkLine.json;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QqMapSearchResult
{
    private Integer status;  
    private String message;  
    private Integer count;  
    private List<QqMapPoiItem> data;
}
