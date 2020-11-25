package com.kl.parkLine.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractEntity
{
    @Temporal(TemporalType.TIMESTAMP)            
    @Column(name="created_date", nullable=false, columnDefinition="datetime comment '创建时间'")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
    @CreatedDate
    @ApiModelProperty(hidden = true)
    private Date createdDate;
    
    @Column(name="created_by", nullable=false, columnDefinition="varchar(255) comment '创建人'")
    @CreatedBy
    @ApiModelProperty(hidden = true)
    private String createdBy;
    
    @Temporal(TemporalType.TIMESTAMP)            
    @Column(name="last_modified_date", nullable=false, columnDefinition="datetime comment '最后更新时间'")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss" )
    @LastModifiedDate
    @ApiModelProperty(hidden = true)
    private Date lastModifiedDate;
    
    @Column(name="last_modified_by", nullable=false, columnDefinition="varchar(255) comment '最后更新人'")
    @LastModifiedBy
    @ApiModelProperty(hidden = true)
    private String lastModifiedBy;

}
