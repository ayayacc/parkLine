package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkLog;
import com.kl.parkLine.entity.QPark;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.ParkPredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.ParkLocationVo;
import com.kl.parkLine.vo.ParkVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParkService
{
    @Autowired
    private IParkDao parkDao;

    @Autowired
    private UserService userService;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    @Autowired
    private ParkPredicates parkPredicates;
    
    @Autowired
    private WKTReader wktReader;
    
    /**
     * 根据停车场编码找到停车场对象
     * @param code 停车场编码
     */
    public Park findOneByCode(String code)
    {
        return parkDao.findOneByCode(code);
    }
    
    /**
     * 找指定位置附近的停车场
     * @param code 停车场编码
     * @throws ParseException 
     */
    public List<ParkLocationVo> findNearby(Point centerPoint, Double distanceKm) throws ParseException
    {
        //找到附近停车场
        List<Map<String, Object>> rows = parkDao.findNearby(centerPoint, distanceKm);
        
        List<ParkLocationVo> ret = new ArrayList<>();
        //遍历所有结果行
        for (Map<String, Object> row : rows)
        {
            Geometry geometry = wktReader.read(row.get("geotext").toString());
            Point point = geometry.getInteriorPoint();
            ParkLocationVo neerByParkVo = ParkLocationVo.builder().parkId((Integer)row.get("park_id"))
                    .code(row.get("code").toString())
                    .name(row.get("name").toString())
                    .totalCnt((Integer)row.get("total_cnt"))
                    .availableCnt((Integer)row.get("available_cnt"))
                    .lng(point.getX())
                    .lat(point.getY())
                    .contact(row.get("contact").toString())
                    .distance((Double)row.get("dist"))
                    .build();
            ret.add(neerByParkVo);
        }
        return ret;
    }
    
    public Park findOneById(Integer parkId) throws BusinessException
    {
        Optional<Park> park = parkDao.findById(parkId);
        if (false == park.isPresent())
        {
            throw new BusinessException(String.format("无效的停车场Id: %d", parkId));
        }
        return park.get();
    }
    
    /**
     * 保存停车场
     * @param park
     * @throws BusinessException 
     */
    public void save(Park park) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == park.getParkId()) //新增数据
        {
            park.setLogs(new ArrayList<ParkLog>());
        }
        else //编辑已有数据
        {
            //编辑停车场，//合并字段
            Optional<Park> parkDst = parkDao.findById(park.getParkId());
            
            if (false == parkDst.isPresent())
            {
                throw new BusinessException(String.format("无效的停车场 Id: %d", park.getParkId()));
            }
            
            //记录不同点
            diff = util.difference(parkDst.get(), park);
            
            BeanUtils.copyProperties(park, parkDst.get(), util.getNullPropertyNames(park));
            
            park = parkDst.get();
        }
        
        //保存数据
        ParkLog log = new ParkLog();
        log.setDiff(diff);
        log.setRemark(park.getChangeRemark());
        if (!StringUtils.isEmpty(diff)  //至少有一项内容时才添加日志
            || !StringUtils.isEmpty(park.getChangeRemark()))
        {
            log.setPark(park);
            park.getLogs().add(log);
        }
        parkDao.save(park);
    }
    
    public Page<ParkVo> fuzzyFindPage(ParkVo parkVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = parkPredicates.fuzzy(parkVo, user);
        
        QPark qPark = QPark.park;
        QueryResults<ParkVo> queryResults = jpaQueryFactory
                .select(Projections.constructor(ParkVo.class, 
                        qPark.parkId,
                        qPark.code,
                        qPark.name,
                        qPark.totalCnt,
                        qPark.availableCnt,
                        qPark.geo,
                        qPark.contact,
                        qPark.enabled))
                .from(qPark)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
}
