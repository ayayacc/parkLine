package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.entity.Order;

@SpringBootTest
public class OrderServiceTest
{
    @Autowired
    private OrderService orderService;
     
    @Test
    @Transactional
    public void testCalAmt()
    {
        Order order = orderService.findOneByOrderId(1);
        order.getPark();
        orderService.calAmt(order);
        assertEquals(20, order.getAmt().intValue());
    }
    
    @Test
    public void calMonth() throws ParseException
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DateTime dateTimeStart = new DateTime(sdf.parse("2020-10-01"));
        DateTime dateTimeEnd = new DateTime(sdf.parse("2021-12-31"));
        int month = (dateTimeEnd.getYear()-dateTimeStart.getYear())*12 + dateTimeEnd.getMonthOfYear()-dateTimeStart.getMonthOfYear() + 1;
        assertEquals(month, 15);
    }
}
