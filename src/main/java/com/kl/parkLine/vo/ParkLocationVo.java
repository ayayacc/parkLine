package com.kl.parkLine.vo;

import java.util.List;

import com.kl.parkLine.entity.ParkFixedFee;
import com.kl.parkLine.entity.ParkStepFee;
import com.kl.parkLine.enums.ChargeType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@Getter
@Setter
@AllArgsConstructor
@ToString
@ApiModel("停车场Vo")
public class ParkLocationVo
{
    @ApiModelProperty("停车场Id")
    private Integer parkId;
    
    @ApiModelProperty("停车场编码")
    private String code;
    
    @ApiModelProperty("名称")
    private String name;
    
    @ApiModelProperty("总可用车位")
    private Integer totalTmpCnt;
    
    @ApiModelProperty("可用车位")
    private Integer availableTmpCnt;
    
    @ApiModelProperty("是否有地面车位") 
    private Boolean hasGroundPlace;
    
    @ApiModelProperty("是否有地下车位") 
    private Boolean hasUndergroundPlace;
    
    @ApiModelProperty("地面月租车位总数量")
    private Integer totalGroundMonthlyCnt;
    
    @ApiModelProperty("当前可用地面月租车位数量")
    private Integer availableGroundMonthlyCnt;
    
    @ApiModelProperty("地下月租车位总数量")
    private Integer totalUndergroundMonthlyCnt;
    
    @ApiModelProperty("当前可用地下月租车位数量")
    private Integer availableUndergroundMonthlyCnt;
    
    @ApiModelProperty("月票说明")
    private String monthlyTktRemark;
    
    @ApiModelProperty("经度")
    private Double lng;
    
    @ApiModelProperty("纬度")
    private Double lat;
    
    @ApiModelProperty("联系方式")
    private String contact;
     
    @ApiModelProperty("距离(KM)")
    private Double distance;
    
    @ApiModelProperty("免费时长(分钟)")
    private Integer freeTimeMin;
    
    @ApiModelProperty("地址")
    private String address;
    
    @ApiModelProperty("计费类型")
    private ChargeType chargeType;
    
    @ApiModelProperty("燃油车固定计费规则")
    private ParkFixedFee fuelFixedFee;
    
    @ApiModelProperty("新能源车固定计费规则")
    private ParkFixedFee newEnergyFixedFee;
    
    @ApiModelProperty("燃油车阶梯计费规则")
    private List<ParkStepFee> fuelStepFees;
    
    @ApiModelProperty("新能源车阶梯计费规则")
    private List<ParkStepFee> newEnergyStepFees;
    
    @ApiModelProperty("计费规则说明")
    private String feeRules;
}
