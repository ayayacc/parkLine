package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BoyueResult
{
    //车牌识别结果
    @JSONField(name="PlateResult")
    PlateResult plateResult;
}
