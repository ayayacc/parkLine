package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.Date;
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

import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.IMonthlyTktDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.MonthlyTkt;
import com.kl.parkLine.entity.MonthlyTktLog;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.QMonthlyTkt;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.MonthlyStatus;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.MonthlyTktPredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.MonthlyTktVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service
public class MonthlyTktService
{
    @Autowired
    private IMonthlyTktDao monthlyTktDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private MonthlyTktPredicates monthlyTktPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 保存一个月票
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    @Transactional
    public void save(MonthlyTkt monthlyTkt) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == monthlyTkt.getMonthlyTktId()) //新增数据
        {
            monthlyTkt.setLogs(new ArrayList<MonthlyTktLog>());
        }
        else//编辑已有数据
        {
            //编辑月票，//合并字段
            Optional<MonthlyTkt> monthlyTktDst = monthlyTktDao.findById(monthlyTkt.getMonthlyTktId());
            
            if (false == monthlyTktDst.isPresent())
            {
                throw new BusinessException(String.format("无效的月票 Id: %d", monthlyTkt.getMonthlyTktId()));
            }
            
            //记录不同点
            diff = util.difference(monthlyTktDst.get(), monthlyTkt);
            
            BeanUtils.copyProperties(monthlyTkt, monthlyTktDst.get(), util.getNullPropertyNames(monthlyTkt));
            
            monthlyTkt = monthlyTktDst.get();
        }
        
        //保存数据
        MonthlyTktLog log = new MonthlyTktLog();
        log.setDiff(diff);
        log.setRemark(monthlyTkt.getChangeRemark());
        log.setMonthlyTkt(monthlyTkt);
        monthlyTkt.getLogs().add(log);
        monthlyTktDao.save(monthlyTkt);
    }
    
    /**
     * 模糊查询
     * @param monthlyTkt  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<MonthlyTktVo> fuzzyFindPage(MonthlyTktVo monthlyTktVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = monthlyTktPredicates.fuzzy(monthlyTktVo, user);
        
        QMonthlyTkt qMonthlyTkt = QMonthlyTkt.monthlyTkt;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qMonthlyTkt.monthlyTktId,
                        qMonthlyTkt.code,
                        qMonthlyTkt.car.carId,
                        qMonthlyTkt.car.carNo,
                        qMonthlyTkt.park.parkId,
                        qMonthlyTkt.park.name,
                        qMonthlyTkt.status,
                        qMonthlyTkt.startDate,
                        qMonthlyTkt.endDate
                )
                .from(qMonthlyTkt)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<MonthlyTktVo> monthlyTktVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> MonthlyTktVo.builder()
                        .monthlyTicketId(tuple.get(qMonthlyTkt.monthlyTktId))
                        .code(tuple.get(qMonthlyTkt.code))
                        .parkId(tuple.get(qMonthlyTkt.park.parkId))
                        .parkName(tuple.get(qMonthlyTkt.park.name))
                        .carId(tuple.get(qMonthlyTkt.car.carId))
                        .carNo(tuple.get(qMonthlyTkt.car.carNo))
                        .startDate(tuple.get(qMonthlyTkt.startDate))
                        .endDate(tuple.get(qMonthlyTkt.endDate))
                        .status(tuple.get(qMonthlyTkt.status).getText())
                        .build()
                        )
                .collect(Collectors.toList());
        return new PageImpl<>(monthlyTktVos, pageable, queryResults.getTotal());
    }
    
    /**
     * 检查指定车辆在指定停车场是否已经有生效的月票
     * @return
     */
    @Transactional(readOnly = true)
    public Boolean existingValid(Car car, Park park, Date startDate, Date endDate)
    {
        return monthlyTktDao.existsByCarCarNoAndParkParkIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                car.getCarNo(), park.getParkId(), MonthlyStatus.payed, endDate, startDate);
    }
}
