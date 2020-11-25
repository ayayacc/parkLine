package com.kl.parkLine.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

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
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
    @CreatedDate
    @ApiModelProperty(hidden = true)
    private Date createdDate;
    
    @Temporal(TemporalType.TIMESTAMP)            
    @Column(name="last_modified_date", nullable=false, columnDefinition="datetime comment '最新更新时间'")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
    @LastModifiedDate
    @ApiModelProperty(hidden = true)
    private Date lastModifiedDate;

}
