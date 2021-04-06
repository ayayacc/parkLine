package com.kl.parkLine.service;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.entity.Park;

@SpringBootTest
public class ParkServiceTest
{
    @Autowired
    private ParkService parkService;
    
    @Autowired
    private IParkDao parkDao;
    
    @Autowired
    private WKTReader wktReader;
     
    @Test
    @Transactional
    public void testCalAmt()
    {
        Park park = parkService.findOneByCode("parkCode01");
        String json = JSON.toJSONString(park);
        assertNotEquals(json.length(), 0);
    }
    
    @Test
    public void testNearBy() throws ParseException
    {
        Geometry point = wktReader.read("POINT (109.411683 24.312310089458418)");
        Point centerPoint = point.getInteriorPoint();
        List<Park> parks = parkDao.findNearby2(centerPoint, 30.0);
        assertNotEquals(0, parks.size());
        assertNotNull(parks.get(0).getFuelFixedFee());
    }
}
