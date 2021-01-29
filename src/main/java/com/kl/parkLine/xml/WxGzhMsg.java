package com.kl.parkLine.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ApiModel("微信公众号消息")
@Builder
@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "xml") 
@NoArgsConstructor
@AllArgsConstructor
public class WxGzhMsg
{
    //开发者微信号
    @ApiModelProperty(required = true, name="开发者微信号")
    @XmlElement(name = "ToUserName")
    @XmlCDATA
    private String toUserName;
    
    //发送方帐号（一个OpenID）
    @ApiModelProperty(required = true, name="发送方帐号（一个OpenID）")
    @XmlElement(name = "FromUserName")
    @XmlCDATA
    private String fromUserName;
    
    //消息创建时间
    @ApiModelProperty(required = true, name="消息创建时间")
    @XmlElement(name = "CreateTime")
    @XmlCDATA
    private Integer createTime;
    
    //消息类型
    @ApiModelProperty(required = true, name="消息类型")
    @XmlElement(name = "MsgType")
    @XmlCDATA
    private String msgType;

    //事件类型，subscribe(订阅)、unsubscribe(取消订阅)
    @ApiModelProperty(required = true, name="事件类型")
    @XmlCDATA
    private String event;
    
    //消息内容
    @ApiModelProperty(required = true, name="content")
    @XmlElement(name = "Content")
    @XmlCDATA
    private String content;
}
