package com.kl.parkLine.json;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestResult
{
    private Integer retCode;  
    private String errMsg;  
    private Object data;
}
