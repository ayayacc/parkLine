package com.kl.parkLine.cmpt;

import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.AliYunCmpt;
import com.kl.parkLine.exception.BusinessException;

@SpringBootTest
public class AliYunCmptTest
{
    @Autowired
    private AliYunCmpt aliYunCmpt;
    
    @Test
    @Transactional
    public void testSendValidCode() throws ParseException, org.locationtech.jts.io.ParseException, BusinessException
    {
        aliYunCmpt.sendValidCode("18077228411", "1234");
    }
    
}
