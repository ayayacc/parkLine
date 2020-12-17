package com.kl.parkLine.json;

import java.util.Date;

import com.alibaba.fastjson.annotation.JSONField;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@ToString
@AllArgsConstructor
@ApiModel("行驶证信息")
public class DrivingLicenseVo
{
    @ApiModelProperty("地址")
    private String address;
    
    @ApiModelProperty("发动机号码")
    private String engineNumber;
    
    @ApiModelProperty("发证日期, 格式:yyyy-MM-dd") 
    @JSONField(format="yyyy-MM-dd")
    private Date issueDate;
    
    @ApiModelProperty("品牌型号")
    private String model;
    
    @ApiModelProperty("所有人名称")
    private String owner;
    
    @ApiModelProperty("车牌号码")
    private String plateNumber;
    
    @ApiModelProperty("注册日期, 格式:yyyy-MM-dd")
    @JSONField(format="yyyy-MM-dd")
    private Date registerDate;
    
    @ApiModelProperty("使用性质")
    private String useCharacter;

    @ApiModelProperty("车辆类型")
    private String vehicleType;
    
    @ApiModelProperty("车辆识别代号")
    private String vin;
}
