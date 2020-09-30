package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.ICarDao;
import com.kl.parkLine.entity.Car;

/**
 * @author chenc
 *
 */
@Service("carService")
public class CarService
{
    @Autowired
    private ICarDao carDao;
    
    @Autowired
    private UserService userService;
    
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
     * @param car 添加的车辆
     */
    @Transactional
    public void addCar(String userName, Car car)
    {
        //检查车辆是否已经存在
        Car carExisted = carDao.findOneByCarNo(car.getCarNo());
        if (null != carExisted)
        {
            car = carExisted;
        }
        
        userService.addCar(userName, car);
        return;
    }
}
