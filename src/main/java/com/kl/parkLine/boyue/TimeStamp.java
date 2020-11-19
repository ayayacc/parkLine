package com.kl.parkLine.boyue;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeStamp
{
    @JsonProperty("Timeval")
    Timeval timeval;
}
