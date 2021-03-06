package com.kl.parkLine.entity;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.alibaba.fastjson.annotation.JSONField;
import com.kl.parkLine.enums.CarType;
import com.kl.parkLine.enums.PlateColor;

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
@Entity(name = "TT_CAR")
@Table(name = "TT_CAR", uniqueConstraints={@UniqueConstraint(columnNames ={"car_no","palte_color"})})
@org.hibernate.annotations.Table(appliesTo = "tt_car",comment = "车辆表")
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Car extends AbstractDateEntity implements java.io.Serializable
{
    @Id
    @Column(name = "car_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carId;
    
    /**
     * 车牌号
     */
    @Column(name = "car_no", length = 16, nullable = false, columnDefinition="varchar(16) comment '车牌号'")
    private String carNo;
    
    /**
     *  车牌颜色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "palte_color", nullable = false, columnDefinition="varchar(64) comment '车牌颜色'")
    private PlateColor plateColor;
    
    /**
     *  车辆类型:燃油车/新能源
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "car_type", nullable = false, columnDefinition="varchar(64) comment '车辆类型:fuel(燃油车)/newEnergy(新能源车)'")
    private CarType carType;
    
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "user_id", columnDefinition="int comment '车辆绑定的用户'")
    @JSONField(serialize = false)
    private User user;

    /**
     * 变动备注
     */
    @ApiModelProperty("变动备注")
    @Transient
    private String changeRemark;
    
    /**
     * 车辆行驶证Id
     */
    @OneToOne(fetch=FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name="license_id", referencedColumnName="driving_license_id", columnDefinition="integer comment '车辆行驶证Id'")
    private DrivingLicense license;
    
    /**
     * 操作记录
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "car", cascade = {CascadeType.ALL})  
    @OrderBy(value = "createdDate desc")
    @JSONField(serialize = false)
    @ApiModelProperty(hidden = true)
    private List<CarLog> logs;
    
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Car [carId=").append(carId).append(", carNo=")
                .append(carNo).append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((carId == null) ? 0 : carId.hashCode());
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
        Car other = (Car) obj;
        if (carId == null)
        {
            if (other.carId != null)
            {
                return false;
            }
        }
        else if (!carId.equals(other.carId))
        {
            return false;
        }
        return true;
    }
    
    
}
