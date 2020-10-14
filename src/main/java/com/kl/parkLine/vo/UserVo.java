package com.kl.parkLine.vo;

import com.kl.parkLine.enums.Gender;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@ApiModel("用户VO")
public class UserVo
{
    @ApiModelProperty("用户Id")
    private Integer userId;
    
    @ApiModelProperty("用户编号，系统自动生成，唯一")
    private String name;
    
    @ApiModelProperty("用户昵称")
    private String nickName;
    
    @ApiModelProperty("手机号码")
    private String mobile;
    
    @ApiModelProperty("性别")
    private Gender gender;
    
    @ApiModelProperty("是否有效")
    private boolean isEnable;
}
