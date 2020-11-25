package com.kl.parkLine.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public abstract class AbstractLog extends AbstractEntity
{
    @Column(name = "remark", length = 512, columnDefinition="varchar(512) comment '备注'")
    @JsonIgnore
    private String remark;
    
    @Column(name = "diff", length = 1024, columnDefinition="varchar(1024) comment '变化部分描述'")
    @JsonIgnore
    private String diff;
    
    
}
