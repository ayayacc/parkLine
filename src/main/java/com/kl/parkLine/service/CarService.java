package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.ICarDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;

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
}
