package com.kl.parkLine.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractDateEntity
{
    @Temporal(TemporalType.TIMESTAMP)            
    @Column(name="created_date", nullable=false, columnDefinition="datetime comment '创建时间'")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
    @CreatedDate
    @ApiModelProperty(hidden = true)
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)            
    @Column(name="last_modified_date", nullable=false, columnDefinition="datetime comment '最新更新时间'")
    @JSONField(format="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
    @LastModifiedDate
    @ApiModelProperty(hidden = true)
    private Date lastModifiedDate;

}
