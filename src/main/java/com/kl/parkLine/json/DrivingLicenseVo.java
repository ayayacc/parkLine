package com.kl.parkLine.json;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel("行驶证信息")
public class DrivingLicenseVo
{
    @ApiModelProperty("地址")
    private String address;
    
    @ApiModelProperty("发动机号码")
    private String engineNumber;
    
    @ApiModelProperty("发证日期, 格式:yyyy-MM-dd") 
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date issueDate;
    
    @ApiModelProperty("品牌型号")
    private String model;
    
    @ApiModelProperty("所有人名称")
    private String owner;
    
    @ApiModelProperty("车牌号码")
    private String plateNumber;
    
    @ApiModelProperty("注册日期, 格式:yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date registerDate;
    
    @ApiModelProperty("使用性质")
    private String useCharacter;

    @ApiModelProperty("车辆类型")
    private String vehicleType;
    
    @ApiModelProperty("车辆识别代号")
    private String vin;
}
