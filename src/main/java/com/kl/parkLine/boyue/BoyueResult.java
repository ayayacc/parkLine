package com.kl.parkLine.boyue;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BoyueResult
{
    //车牌识别结果
    @JsonProperty("PlateResult")
    PlateResult plateResult;
}
