package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.CompareUtil;
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
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
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
    private CompareUtil compareUtil;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 根据停车场编码找到停车场对象
     * @param code 停车场编码
     */
    @Transactional
    public Park findOneByCode(String code)
    {
        return parkDao.findOneByCode(code);
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
            diff = compareUtil.difference(parkDst.get(), park);
            
            BeanUtils.copyProperties(park, parkDst.get(), compareUtil.getNullPropertyNames(park));
            
            park = parkDst.get();
        }
        
        //保存数据
        ParkLog log = new ParkLog();
        log.setDiff(diff);
        log.setPark(park);
        park.getLogs().add(log);
        parkDao.save(park);
    }
    
    @Transactional(readOnly = true)
    public Page<ParkVo> fuzzyFindPage(Park park, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = ParkPredicates.fuzzySearch(park, user);
        
        QPark qPark = QPark.park;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
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
                        qPark.maxAmt
                )
                .from(qPark)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<ParkVo> parkVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> ParkVo.builder()
                        .parkId(tuple.get(qPark.parkId))
                        .code(tuple.get(qPark.code))
                        .name(tuple.get(qPark.name))
                        .totalCnt(tuple.get(qPark.totalCnt))
                        .availableCnt(tuple.get(qPark.availableCnt))
                        .geo(tuple.get(qPark.geo))
                        .contact(tuple.get(qPark.contact))
                        .enabled(tuple.get(qPark.enabled))
                        .freeTime(tuple.get(qPark.freeTime))
                        .timeLev1(tuple.get(qPark.timeLev1))
                        .priceLev1(tuple.get(qPark.priceLev1))
                        .priceLev1(tuple.get(qPark.priceLev1))
                        .timeLev2(tuple.get(qPark.timeLev2))
                        .priceLev2(tuple.get(qPark.priceLev2))
                        .build()
                        )
                .collect(Collectors.toList());
        return new PageImpl<>(parkVos, pageable, queryResults.getTotal());
    }
}
