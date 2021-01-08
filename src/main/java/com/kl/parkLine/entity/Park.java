package com.kl.parkLine.entity;

import java.math.BigDecimal;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.Where;
import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.ChargeType;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 停车场
 *
 * <p>
 * 停车场数据
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "TC_PARK")
@org.hibernate.annotations.Table(appliesTo = "tc_park", comment = "停车场")
@EntityListeners(
{ AuditingEntityListener.class })
public class Park extends AbstractDateEntity implements java.io.Serializable
{
    @Id
    @Column(name = "park_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer parkId;

    /**
     * 停车场编号
     */
    @NeedToCompare(name = "编号")
    @Column(name = "code", nullable = false, length = 64, unique = true, columnDefinition = "varchar(64) comment '停车场唯一编号'")
    private String code;

    /**
     * 停车场名称
     */
    @NeedToCompare(name = "名称")
    @Column(name = "name", nullable = false, length = 64, unique = true, columnDefinition = "varchar(64) comment '停车场名称'")
    private String name;

    /**
     * 停车场变动备注
     */
    @ApiModelProperty("停车场变动备注")
    @Transient
    private String changeRemark;

    /**
     * 临停车位总数量
     */
    @NeedToCompare(name = "临停车位总数")
    @Column(name = "total_tmp_cnt", nullable = false, columnDefinition = "int comment '临停车位总数'")
    private Integer totalTmpCnt;

    /**
     * 当前可用临停车位数量
     */
    @NeedToCompare(name = "可用临停车位数")
    @Column(name = "available_tmp_cnt", nullable = false, columnDefinition = "int comment '可用临停车位数'")
    private Integer availableTmpCnt;

    /**
     * 经纬度
     */
    @Column(name = "geo", columnDefinition = "geometry comment '经纬度'")
    private Point geo;;

    /**
     * 联系方式
     */
    @NeedToCompare(name = "联系方式")
    @Column(name = "contact", nullable = false, length = 16, columnDefinition = "varchar(16) comment '联系方式'")
    private String contact;

    /**
     * 省
     */
    @NeedToCompare(name = "省")
    @Column(name = "province", length = 16, columnDefinition = "varchar(16) comment '省'")
    private String province;

    /**
     * 市
     */
    @NeedToCompare(name = "市")
    @Column(name = "city", length = 16, columnDefinition = "varchar(16) comment '市'")
    private String city;

    /**
     * 详细地址
     */
    @NeedToCompare(name = "详细地址")
    @Column(name = "address", length = 128, nullable = false, columnDefinition = "varchar(128) comment '详细地址'")
    private String address;

    /**
     * 是否有效
     */
    @NeedToCompare(name = "是否有效")
    @Column(name = "enabled", length = 4, nullable = false, columnDefinition = "varchar(4) comment '是否有效,Y/N'")
    private String enabled;

    /**
     * 计费类型: 固定计费/阶梯计费
     */
    @Column(name = "charge_type", nullable = false, columnDefinition = "varchar(255) comment '计费类型: 固定计费/阶梯计费'")
    @Enumerated(EnumType.STRING)
    private ChargeType chargeType;

    /**
     * 白名单
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @Where(clause = "park_car_type='white'")
    @JSONField(serialize = false)
    private List<ParkCarItem> whiteList;

    /**
     * 黑名单
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @Where(clause = "park_car_type='black'")
    @JSONField(serialize = false)
    private List<ParkCarItem> blackList;

    /**
     * 白名单是否包含白色车牌
     */
    @Column(name = "white_list_include_white_plate", nullable = false, columnDefinition = "bool default true comment '白色车牌是否免费'")
    private Boolean whiteListIncludeWhitePlate;

    /**
     * 黑名单是否包含欠费车辆
     */
    @Column(name = "black_list_include_owe", nullable = false, columnDefinition = "bool default true comment '黑名单是否包含欠费车辆'")
    private Boolean blackListIncludeOwe;

    /**
     * 燃油车固定计费规则
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "fuel_fixed_fee_id", referencedColumnName = "park_fixed_fee_id", columnDefinition = "integer comment '燃油车固定计费规则'")
    private ParkFixedFee fuelFixedFee;

    /**
     * 燃油车特殊时段计费规则
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @Where(clause = "car_type='fuel'")
    @OrderBy(value = "startMin asc")
    @JSONField(serialize = false)
    private List<ParkSpecialFee> fuelSpecialFees;

    /**
     * 燃油车阶梯计费规则
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @Where(clause = "car_type='fuel'")
    @OrderBy(value = "startMin asc")
    @JSONField(serialize = false)
    private List<ParkStepFee> fuelStepFees;

    /**
     * 新能源车固定计费规则
     */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "new_energy_fixed_fee_id", referencedColumnName = "park_fixed_fee_id", columnDefinition = "integer comment '新能源车固定计费规则'")
    private ParkFixedFee newEnergyFixedFee;

    /**
     * 燃油车特殊时段计费规则
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @Where(clause = "car_type='newEnergy'")
    @OrderBy(value = "startMin asc")
    @JSONField(serialize = false)
    private List<ParkSpecialFee> newEnergySpecialFees;

    /**
     * 新能源车阶梯计费规则
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @Where(clause = "car_type='newEnergy'")
    @OrderBy(value = "startMin asc")
    @JSONField(serialize = false)
    private List<ParkStepFee> newEnergyStepFees;

    @NeedToCompare(name = "月票说明")
    @Column(name = "monthly_tkt_remark", length = 1024, columnDefinition = "varchar(1024) comment '月票说明'")
    private String monthlyTktRemark;

    /**
     * 是否有地面车位
     */
    @Column(name = "has_ground_place", nullable = false, columnDefinition = "bool default false comment '是否有地面车位'")
    private Boolean hasGroundPlace;

    @NeedToCompare(name = "燃油车地面月票单价")
    @Column(name = "fuel_ground_monthly_price", precision = 8, scale = 2, columnDefinition = "decimal(8,2) comment '燃油车地面月票单价'")
    private BigDecimal fuelGroundMonthlyPrice;

    @NeedToCompare(name = "新能源车地面月票单价")
    @Column(name = "new_energy_ground_monthly_price", precision = 8, scale = 2, columnDefinition = "decimal(8,2) comment '新能源车地面月票单价'")
    private BigDecimal newEnergyGroundMonthlyPrice;

    /**
     * 地面月租车位总数量
     */
    @NeedToCompare(name = "地面月租车位总数量")
    @Column(name = "total_ground_monthly_cnt", nullable = false, columnDefinition = "int comment '地面月租车位总数量'")
    private Integer totalGroundMonthlyCnt;

    /**
     * 当前可用地面月租车位数量
     */
    @NeedToCompare(name = "当前可用地面月租车位数量")
    @Column(name = "available_ground_monthly_cnt", nullable = false, columnDefinition = "int comment '当前可用地面月租车位数量'")
    private Integer availableGroundMonthlyCnt;

    /**
     * 是否有地下车位
     */
    @Column(name = "has_underground_place", nullable = false, columnDefinition = "bool default false comment '是否有地下车位'")
    private Boolean hasUndergroundPlace;

    @NeedToCompare(name = "燃油车地下月票单价")
    @Column(name = "fuel_underground_monthly_price", precision = 8, scale = 2, columnDefinition = "decimal(8,2) comment '燃油车地下月票单价'")
    private BigDecimal fuelUndergroundMonthlyPrice;

    @NeedToCompare(name = "新能源车地下月票单价")
    @Column(name = "new_energy_underground_monthly_price", precision = 8, scale = 2, columnDefinition = "decimal(8,2) comment '新能源车地下月票单价'")
    private BigDecimal newEnergyUndergroundMonthlyPrice;

    /**
     * 地下月租车位总数量
     */
    @NeedToCompare(name = "地下月租车位总数量")
    @Column(name = "total_underground_monthly_cnt", nullable = false, columnDefinition = "int comment '地下月租车位总数量'")
    private Integer totalUndergroundMonthlyCnt;

    /**
     * 当前可用地下月租车位数量
     */
    @NeedToCompare(name = "当前可用地下月租车位数量")
    @Column(name = "available_underground_monthly_cnt", nullable = false, columnDefinition = "int comment '当前可用地下月租车位数量'")
    private Integer availableUndergroundMonthlyCnt;

    /**
     * 停车场设备
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @OrderBy(value = "name desc")
    @JSONField(serialize = false)
    private List<Device> devices;

    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "park", cascade =
    { CascadeType.ALL })
    @OrderBy(value = "createdDate desc")
    @JSONField(serialize = false)
    private List<ParkLog> logs;

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Park [parkId=").append(parkId).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((parkId == null) ? 0 : parkId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Park other = (Park) obj;
        if (parkId == null)
        {
            if (other.parkId != null)
            {
                return false;
            }
        }
        else if (!parkId.equals(other.parkId))
        {
            return false;
        }
        return true;
    }
}
