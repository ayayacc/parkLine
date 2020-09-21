package com.kl.parkLine.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name="TC_DICT")
@SuppressWarnings("serial")
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@EntityListeners({AuditingEntityListener.class})
public class Dict extends AbstractEntity implements java.io.Serializable
{
    @Id
    @Column(name="dict_id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer dictId;
    
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_dict_id")
    @JsonIgnore 
    private Dict parentDict;
    
    @Column(name = "code", nullable = false, length = 255, unique = true)
    private String code;
    
    @Column(name = "text", nullable = false, length = 255)
    private String text;
    
    @Column(name = "enabled", nullable = false)
    private String enabled;
    
    @Column(name = "sort_idx", nullable = false)
    private String sortIdx;
    
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentDict", cascade = {CascadeType.PERSIST,CascadeType.MERGE,CascadeType.REFRESH})  
    @OrderBy("sortIdx asc") 
    private Set<Dict> childDicts; 

    
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((code == null) ? 0 : code.hashCode());
        result = prime * result + ((dictId == null) ? 0 : dictId.hashCode());
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
        Dict other = (Dict) obj;
        if (code == null)
        {
            if (other.code != null)
            {
                return false;
            }
        }
        else if (!code.equals(other.code))
        {
            return false;
        }
        if (dictId == null)
        {
            if (other.dictId != null)
            {
                return false;
            }
        }
        else if (!dictId.equals(other.dictId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Dict [dictId=").append(dictId).append(", code=")
                .append(code).append("]");
        return builder.toString();
    }

    

}
