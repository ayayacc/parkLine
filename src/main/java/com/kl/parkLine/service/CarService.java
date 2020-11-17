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
import com.kl.parkLine.dao.ICarDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.CarLog;
import com.kl.parkLine.entity.QCar;
import com.kl.parkLine.entity.QUser;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CarType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.enums.RoleType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.BindCarParam;
import com.kl.parkLine.predicate.CarPredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.CarVo;
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
public class CarService
{
    @Autowired
    private UserService userService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private ICarDao carDao;
    
    @Autowired
    private CarPredicates carPredicates;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 保存一个订单
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    public void save(Car car) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == car.getCarId()) //新增数据
        {
            car.setLogs(new ArrayList<CarLog>());
        }
        else//编辑已有数据
        {
            //编辑订单，//合并字段
            Optional<Car> carDst = carDao.findById(car.getCarId());
            
            if (false == carDst.isPresent())
            {
                throw new BusinessException(String.format("无效的车辆 Id: %d", car.getCarId()));
            }
            
            //记录不同点
            diff = util.difference(carDst.get(), car);
            
            BeanUtils.copyProperties(car, carDst.get(), util.getNullPropertyNames(car));
            
            car = carDst.get();
        }
        
        //保存数据
        CarLog log = new CarLog();
        log.setDiff(diff);
        log.setRemark(car.getChangeRemark());
        if (!StringUtils.isEmpty(diff)  //至少有一项内容时才添加日志
            || !StringUtils.isEmpty(car.getChangeRemark()))
        {
            log.setCar(car);
            car.getLogs().add(log);
        }
        carDao.save(car);
    }
    
    /**
     * 总是返回一个Car的数据库对象，如果数据库中没有，则新增车辆
     * @param carNo 车牌号码
     * @throws BusinessException 
     */
    public Car getCar(String carNo, PlateColor plateColor) throws BusinessException
    {
        Car car = carDao.findOneByCarNoAndPlateColor(carNo, plateColor);
        if (null == car)
        {
            car = new Car();
            car.setCarNo(carNo);
            car.setPlateColor(plateColor);
            car.setCarType(CarType.fuel);
            if (plateColor.equals(PlateColor.green))
            {
                car.setCarType(CarType.newEnergy);
            }
            this.save(car);
        }
        return car;
    }
    
    /**
     * 通过Id查找唯一车辆
     * @param carId 车辆Id
     * @throws BusinessException 
     */
    public Car findOneById(Integer carId)
    {
        Optional<Car> car = carDao.findById(carId);
        return car.get();
    }
    
    /**
     * 添加车辆，并且绑定到当前用户
     * @param userName 添加的用户名称
     * @param carNo 添加的车牌号码
     * @throws BusinessException 
     */
    public void bind(String userName, BindCarParam bindCarParam) throws BusinessException
    {
        User user = userService.findByName(userName);
        String carNo = bindCarParam.getCarNo();
        PlateColor plateColor = bindCarParam.getPlateColor();
        
        //检查车辆是否已经存在
        Car car = getCar(carNo, plateColor);
        if (null != car.getUser()) //车辆已经绑定到其他用户
        {
            if (user.equals(car.getUser()))
            {
                throw new BusinessException(String.format("%s 已经绑定到您的账户下，请勿重复绑定", carNo));
            }
            else 
            {
                throw new BusinessException(String.format("%s 已经被其他用户绑定，请联系TA解绑后再进行绑定", carNo));
            }
        }
        car.setUser(user);
        car.setChangeRemark(String.format("绑定到用户: %s", user.getName()));
        this.save(car);
        
        //将涉及到此车辆的无主订单绑定到该用户
        orderService.setOrderOwnerByCar(car);
        return;
    }
    
    /**
     * 添加车辆，并且绑定到当前用户
     * @param userName 添加的用户名称
     * @param carNo 添加的车牌号码
     * @throws BusinessException 
     */
    public void unbind(Car car) throws BusinessException
    {
        //检查车辆是否已经存在
        if (null == car.getUser()) //如果车辆当前并未绑定到用户
        {
            return;
        }
        else
        {
            car.setChangeRemark(String.format("从用户解绑: %s", car.getUser().getName()));
            car.setUser(null);
        }
        
        this.save(car);
        return;
    }
    
    /**
     * 模糊匹配车辆
     * @param car  
     * @param pageable
     * @param auth
     * @return
     */
    public Page<CarVo> fuzzyFindPage(CarVo carVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = carPredicates.fuzzy(carVo, user);
        
        QueryResults<CarVo> queryResults = jpaQueryFactory
                .select(Projections.constructor(CarVo.class, 
                        QCar.car.carId,
                        QCar.car.carNo,
                        QUser.user.name))
                .from(QCar.car).leftJoin(QUser.user).on(QCar.car.user.eq(QUser.user))
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
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
