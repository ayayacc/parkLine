package com.kl.parkLine.component;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import com.kl.parkLine.feign.IMapFeignClient;
import com.kl.parkLine.json.QqMapSearchResult;

@Component
public class MapCmpt
{
    @Value("${map.qq.accessKey}")
    private String accessKey;
    
    @Value("${map.qq.secretKey}")
    private String secretKey;
    
    private final String KEY_WORD = "停车场";
    
    @Autowired
    IMapFeignClient mapFeignClient;
    
    /**
     * 查找附近的停车场
     * @param boundary
     * @param keyword
     * @return
     */
    public QqMapSearchResult search(Point centerPoint, Double distanceKm)
    {
        //nearby(39.908491,116.374328,1000)
        Integer distanceM = (int) (distanceKm*1000);
        String boundary = String.format("nearby(%f,%f,%d)", centerPoint.getY(), centerPoint.getX(), distanceM);
        String target = String.format("/ws/place/v1/search?boundary=%s&key=%s&keyword=%s&orderby=_distance%s", 
                boundary, accessKey, KEY_WORD, secretKey);
        String sig = DigestUtils.md5DigestAsHex(target.getBytes());
        return mapFeignClient.search(boundary, accessKey, KEY_WORD, sig);
    }
}
