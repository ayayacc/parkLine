package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals(0, order.getAmt().intValue());
    }
}
