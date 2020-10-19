package com.kl.parkLine.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.ICarDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.QCar;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.RoleType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.CarPredicates;
import com.kl.parkLine.vo.CarVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service
public class CarService
{
    @Autowired
    private UserService userService;
    
    @Autowired
    private ICarDao carDao;
    
    @Autowired
    private CarPredicates carPredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 总是返回一个Car的数据库对象，如果数据库中没有，则新增车辆
     * @param carNo 车牌号码
     */
    @Transactional(readOnly = true)
    public Car getCar(String carNo)
    {
        Car car = carDao.findOneByCarNo(carNo);
        if (null == car)
        {
            car = new Car();
            car.setCarNo(carNo);
            carDao.save(car);
        }
        return car;
    }
    
    /**
     * 添加车辆，并且绑定到当前用户
     * @param userName 添加的用户名称
     * @param carNo 添加的车牌号码
     * @throws BusinessException 
     */
    @Transactional
    public void bind(String userName, String carNo) throws BusinessException
    {
        User user = userService.findByName(userName);
        //检查车辆是否已经存在
        Car car = carDao.findOneByCarNo(carNo);
        if (null == car)
        {
            car = new Car();
            car.setCarNo(carNo);
        }
        else if (null != car.getUser()) //车辆已经绑定到其他用户
        {
            String mobile = car.getUser().getMobile();
            String right = mobile.substring(mobile.length()-4); //取手机尾号后四位
            throw new BusinessException(String.format("% 已经被手机尾号: %s 用户绑定，请联系TA解绑后再进行绑定", carNo, right));
        }
        car.setUser(user);
        carDao.save(car);
        return;
    }
    
    /**
     * 添加车辆，并且绑定到当前用户
     * @param userName 添加的用户名称
     * @param carNo 添加的车牌号码
     * @throws BusinessException 
     */
    @Transactional
    public void unbind(String carNo)
    {
        //检查车辆是否已经存在
        Car car = carDao.findOneByCarNo(carNo);
        if (null == car)
        {
            car = new Car();
            car.setCarNo(carNo);
        }
        else if (null == car.getUser()) //如果车辆当前并未绑定到用户
        {
            return;
        }
        car.setUser(null);
        carDao.save(car);
        return;
    }
    
    /**
     * 模糊匹配车辆
     * @param car  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<CarVo> fuzzyFindPage(CarVo carVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = carPredicates.fuzzy(carVo, user);
        
        QCar qCar = QCar.car;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qCar.carId,
                        qCar.carNo
                )
                .from(qCar)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<CarVo> carVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> CarVo.builder()
                        .carId(tuple.get(qCar.carId))
                        .carNo(tuple.get(qCar.carNo))
                        .build()
                        )
                .collect(Collectors.toList());
        return new PageImpl<>(carVos, pageable, queryResults.getTotal());
    }
    
    /**
     * 校验当前登录用户是否可以访问data 数据
     * 公司类型的账户可以看所有车辆
     * 其他用户只能看自己车辆
     * @param data 期待访问的数据
     * @param auth 当前登录的用户
     * @param permission 需要的权限
     * @return 是否有权限
     */
    @Transactional(readOnly = true)
    public boolean hasPermission(Car reqData, User logonUser, String permission) 
    {
        //公司类型的账户可以看所有车辆
        if (logonUser.hasRoleType(RoleType.company))
        {
            return true;
            
        }
        else //其他用户只能看自己车辆
        {
            return reqData.getUser().getName().equalsIgnoreCase(logonUser.getName());
        }
    }
}
