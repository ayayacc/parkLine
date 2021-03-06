package com.kl.parkLine.boyue;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class PlateResult
{
    //车牌号码
    @JSONField(name="license")
    private String plateNo;
    
    //车牌颜色
    private Integer colorType;
    
    //当前结果的触发类型：1：自动触发类型、2：外部输入触 发（IO 输入）、4：软件触发（SDK）、8：虚拟线圈触发
    private Integer triggerType;
    
    //识别结果车牌ID
    @JSONField(name="plateid")
    private Integer plateId;
    
    //截图
    private String imageFile;
    
    //截图图片大小
    private Integer imageFileLen;
    
    //设备离线状态，0：在线，1：离线
    private Integer isoffline;
    
    //识别结果对应帧的时间戳
    private TimeStamp timeStamp;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("PlateResult [plateNo=").append(plateNo)
                .append(", colorType=").append(colorType)
                .append(", triggerType=").append(triggerType)
                .append(", plateId=").append(plateId).append(", isoffline=")
                .append(isoffline).append(", timeStamp=").append(timeStamp)
                .append("]");
        return builder.toString();
    }
    
}
