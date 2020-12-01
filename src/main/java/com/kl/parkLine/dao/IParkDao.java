package com.kl.parkLine.dao;

import java.util.List;
import java.util.Map;

import org.locationtech.jts.geom.Point;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Park;

@Repository
public interface IParkDao extends JpaRepository<Park, Integer>
{
    public Park findOneByCode(String code);
    
    @Query(value = "select *,st_astext(geo) as geotext, round(st_distance_sphere(geo, :centerPoint)/1000, 2) as dist from TC_PARK where round(st_distance_sphere(geo, :centerPoint)/1000, 2)<:distanceKm and enabled='Y' order by dist desc", nativeQuery = true)
    List<Map<String, Object>> findNearby(@Param("centerPoint")Point centerPoint, @Param("distanceKm")Double distanceKm);
    

}