package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.Optional;

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
import com.kl.parkLine.vo.ParkVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service("parkService")
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
    
    /**
     * 根据停车场编码找到停车场对象
     * @param code 停车场编码
     */
    @Transactional
    public Park findOneByCode(String code)
    {
        return parkDao.findOneByCode(code);
    }
    
    @Transactional
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
     * 编辑停车场
     * @param park
     * @throws BusinessException 
     */
    @Transactional
    public void edit(Park park, String userName) throws BusinessException
    {
        this.save(park);
    }
    
    /**
     * 保存停车场
     * @param park
     * @throws BusinessException 
     */
    @Transactional
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
    
    @Transactional(readOnly = true)
    public Page<ParkVo> fuzzyFindPage(ParkVo parkVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = parkPredicates.fuzzy(parkVo, user);
        
        QPark qPark = QPark.park;
        QueryResults<ParkVo> queryResults = jpaQueryFactory
                .select(Projections.bean(ParkVo.class, 
                        qPark.parkId,
                        qPark.code,
                        qPark.name,
                        qPark.totalCnt,
                        qPark.availableCnt,
                        qPark.geo,
                        qPark.contact,
                        qPark.enabled,
                        qPark.freeTime,
                        qPark.timeLev1,
                        qPark.priceLev1,
                        qPark.timeLev2,
                        qPark.priceLev2,
                        qPark.maxAmt))
                .from(qPark)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
}
