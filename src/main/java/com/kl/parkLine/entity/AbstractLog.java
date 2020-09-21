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
    @Column(name = "remark", length = 512)
    @JsonIgnore
    private String remark;
    
    @Column(name = "diff", length = 1024)
    @JsonIgnore
    private String diff;
    
    
}
