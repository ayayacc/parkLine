package com.kl.parkLine.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * 事件图片
 *
 * <p>车辆数据
 * @author chenc 2020年9月11日
 * @see
 * @since 1.0
 */
@Getter
@Setter
@SuppressWarnings("serial")
@Entity
@Table(name = "TT_EVENT_PIC")
@DynamicUpdate
@DynamicInsert
public class EventPic implements java.io.Serializable
{
    @Id
    @Column(name = "event_pic_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer eventPicId;
    
    /**
     * 事件
     */
    @ManyToOne(optional = false, fetch = FetchType.LAZY) 
    @JoinColumn(name = "event_id")
    private Event event;

    /**
     * 图片url
     */
    @Column(name = "pic_url", length = 128, nullable = false)
    private String picUrl;
    
    /**
     * 图片类型
     */
    @Column(name = "pic_type", length = 16, nullable = false)
    private String picType;

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((eventPicId == null) ? 0 : eventPicId.hashCode());
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
        EventPic other = (EventPic) obj;
        if (eventPicId == null)
        {
            if (other.eventPicId != null)
            {
                return false;
            }
        }
        else if (!eventPicId.equals(other.eventPicId))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "EventPic [eventPicId=" + eventPicId + "]";
    }
    
    
}
