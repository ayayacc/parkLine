package com.kl.parkLine.cmpt;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.text.ParseException;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.MapCmpt;
import com.kl.parkLine.json.QqMapSearchResult;

@SpringBootTest
public class MapCmptTest
{
    @Autowired
    private MapCmpt mapCmpt;
    
    @Autowired
    private WKTReader wktReader;
    
    @Test
    @Transactional
    public void testFindNearByPark() throws ParseException, org.locationtech.jts.io.ParseException
    {
        Geometry geometry = wktReader.read("POINT(109.411683 24.312310089458418)");
        Point point = geometry.getInteriorPoint();
        QqMapSearchResult result = mapCmpt.search(point, 5d);
        assertNotNull(result);
    }
    
}
