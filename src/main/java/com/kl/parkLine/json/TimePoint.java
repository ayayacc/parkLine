package com.kl.parkLine.json;

import org.joda.time.DateTime;

import com.kl.parkLine.entity.Order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimePoint implements Comparable<TimePoint>
{
    private String key;
    private Order order;
    private DateTime dateTime;
    
    @Override
    public int compareTo(TimePoint o)
    {
        // TODO Auto-generated method stub
        return this.getDateTime().compareTo(o.dateTime);
    }
}