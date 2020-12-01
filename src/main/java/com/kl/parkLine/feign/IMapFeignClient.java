package com.kl.parkLine.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kl.parkLine.json.QqMapSearchResult;

@FeignClient(name="mapFeignClient", url="https://apis.map.qq.com", fallback=FeignClientFallback.class)
public interface IMapFeignClient
{
    //"https://apis.map.qq.com/ws/place/v1/search?boundary={boundary}&key={key}&keyword={keyword}&sig={sig}";
    @GetMapping(value = "/ws/place/v1/search?boundary={boundary}&key={key}&keyword={keyword}&orderby=_distance&sig={sig}")
    public QqMapSearchResult search(@RequestParam("boundary")String boundary, 
            @RequestParam("key")String key, @RequestParam("keyword")String keyword,
            @RequestParam("sig")String sig);
}