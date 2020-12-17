package com.kl.parkLine.json;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("事件处理结果")
public class EventResult
{
    //是否开闸
    private Boolean open;
    
    //显示屏内容
    private String content;
    
    public static EventResult open(String content) 
    {
        EventResult result = new EventResult();
        result.setOpen(true);
        result.setContent(content);
        return result;
    }
    
    public static EventResult notOpen(String content) 
    {
        EventResult result = new EventResult();
        result.setOpen(false);
        result.setContent(content);
        return result;
    }
    
    public static EventResult notOpen() 
    {
        EventResult result = new EventResult();
        result.setOpen(false);
        return result;
    }
}
