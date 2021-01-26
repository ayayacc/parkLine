package com.kl.parkLine.cmpt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.WxCmpt;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.WxSendMsgResult;
import com.kl.parkLine.service.OrderService;

@SpringBootTest
public class WxCmptTest
{
    @Autowired
    private WxCmpt wxCmpt;
    
    @Autowired
    private OrderService orderService;
    
    @Test
    @Transactional
    public void testSendMonthlyTktExpireMsg() throws BusinessException
    {
        Order order = orderService.findOneByOrderId(3);
        WxSendMsgResult result = wxCmpt.sendMonthlyTktExpireNote(order);
        assertEquals(0, result.getErrcode());
    }
    
}
