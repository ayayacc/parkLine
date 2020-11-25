package com.kl.parkLine.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 车辆信息
 *
 * <p>车辆数据
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity(name = "TT_DRIVING_LICENSE")
@org.hibernate.annotations.Table(appliesTo = "tt_driving_license",comment = "车辆行驶证信息")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class DrivingLicense extends AbstractDateEntity implements java.io.Serializable
{
    @Id
    @Column(name = "driving_license_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer drivingLicenseId;
    
    @ApiModelProperty("地址")
    @Column(name = "address", length = 128, columnDefinition="varchar(128) comment '行驶证地址'")
    private String address;
    
    @ApiModelProperty("发动机号码")
    @Column(name = "engine_number", length = 64, columnDefinition="varchar(64) comment '发动机号码'")
    private String engineNumber;
    
    @ApiModelProperty("发证日期, 格式:yyyy-MM-dd") 
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)            
    @Column(name = "issue_date", nullable = false, columnDefinition="date comment '发证日期, 格式:yyyy-MM-dd'")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date issueDate;
    
    @ApiModelProperty("品牌型号")
    @Column(name = "model", length = 64, columnDefinition="varchar(64) comment '品牌型号'")
    private String model;
    
    @ApiModelProperty("所有人名称")
    @Column(name = "owner", length = 32, columnDefinition="varchar(32) comment '所有人名称'")
    private String owner;
    
    @ApiModelProperty("注册日期, 格式:yyyy-MM-dd")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Temporal(TemporalType.DATE)            
    @Column(name = "register_date", nullable = false, columnDefinition="date comment '注册日期, 格式:yyyy-MM-dd'")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date registerDate;
    
    @ApiModelProperty("使用性质")
    @Column(name = "use_character", length = 64, columnDefinition="varchar(64) comment '使用性质'")
    private String useCharacter;

    @ApiModelProperty("车辆类型")
    @Column(name = "vehicle_type", length = 64, columnDefinition="varchar(64) comment '车辆类型'")
    private String vehicleType;
    
    @ApiModelProperty("车辆识别代号")
    @Column(name = "vin", length = 64, columnDefinition="varchar(64) comment '车辆识别代号'")
    private String vin;
    
    @Column(name = "img_code", length = 128, columnDefinition="varchar(128) comment '图片信息'")
    private String imgCode;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((drivingLicenseId == null) ? 0
                : drivingLicenseId.hashCode());
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
        DrivingLicense other = (DrivingLicense) obj;
        if (drivingLicenseId == null)
        {
            if (other.drivingLicenseId != null)
            {
                return false;
            }
        }
        else if (!drivingLicenseId.equals(other.drivingLicenseId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("DrivingLicense [drivingLicenseId=")
                .append(drivingLicenseId).append("]");
        return builder.toString();
    }
    
}
