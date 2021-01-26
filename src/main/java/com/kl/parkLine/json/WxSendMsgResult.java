package com.kl.parkLine.json;

import com.alibaba.fastjson.annotation.JSONField;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class WxSendMsgResult extends WxResultBase
{
    @JSONField(name="msgid")
    private Long msgId;  
}
