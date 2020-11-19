package com.kl.parkLine.xlt;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class XltEvtResult
{
    private Integer errorcode;
    private String message;
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("BoyueResult [errorcode=").append(errorcode)
                .append(", message=").append(message).append("]");
        return builder.toString();
    }
    
}
