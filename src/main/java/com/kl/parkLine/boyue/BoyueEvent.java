package com.kl.parkLine.boyue;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class BoyueEvent
{
    @JsonProperty("AlarmInfoPlate")
    AlarmInfoPlate alarmInfoPlate;
}
