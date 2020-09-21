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
    
    /**
     * 总是返回一个Car的数据库对象，如果数据库中没有，则新增车辆
     * @param carNo 车牌号码
     */
    @Transactional
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
}
