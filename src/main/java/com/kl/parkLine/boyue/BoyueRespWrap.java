package com.kl.parkLine.boyue;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BoyueRespWrap
{
    @JsonProperty("Response_AlarmInfoPlate")
    private BoyueResp boyueResp;
}
