package com.kl.parkLine.cmpt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kl.parkLine.component.AliYunCmpt;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.DrivingLicenseVo;

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
    
    @Test
    @Transactional
    public void testRecognizeDrivingLicense() throws ParseException, org.locationtech.jts.io.ParseException, BusinessException
    {
        try
        {
            FileInputStream is = new FileInputStream("E:\\MY_PARK\\微信图片_20201125165551.jpg");
            MultipartFile file = new MockMultipartFile("test.png", is);
            DrivingLicenseVo vo = aliYunCmpt.recognizeDrivingLicense(file);
            assertEquals("桂BJ1012", vo.getPlateNumber());
        }
        catch (Exception e)
        {
            e.getMessage();
        }
    }
    
}
