package com.kl.parkLine.json;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class ContentLines
{
    //第1行
    private String line1;
    //第2行
    private String line2;
    //第3行
    private String line3;
    //第4行
    private String line4;
    //语音行
    private String voice;
    //显示次数
    private Byte dr;
}
