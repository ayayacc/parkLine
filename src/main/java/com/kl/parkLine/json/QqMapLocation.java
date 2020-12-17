package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class QqMapLocation extends WxResultBase
{
    private Double lat;  
    private Double lng;  
}
