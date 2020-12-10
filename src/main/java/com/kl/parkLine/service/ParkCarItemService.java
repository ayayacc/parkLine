package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IParkCarItemDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.ParkCarType;
import com.kl.parkLine.enums.PlateColor;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParkCarItemService
{
    @Autowired
    private IParkCarItemDao parkCarItemDao;
    
    @Autowired
    private OrderService orderService;

    /**
     * 检查车辆是否在白名单中
     * @param code 停车场编码
     */
    public boolean existsInWhiteList(Park park, Car car)
    {
        if (park.getWhiteListIncludeWhitePlate() && car.getPlateColor().equals(PlateColor.white))
        {
            return true;
        }
        return parkCarItemDao.existsByParkAndCarAndParkCarType(park, car, ParkCarType.white);
    }
    
    /**
     * 检查车辆是否在黑名单中
     * @param code 停车场编码
     */
    public boolean existsInBlackList(Park park, Car car)
    {
        if (park.getBlackListIncludeOwe() //黑名单包含欠费车辆
                &&orderService.existsByTypeAndCarAndStatus(OrderType.parking, car, OrderStatus.needToPay))
        {
            return true;
        }
        return parkCarItemDao.existsByParkAndCarAndParkCarType(park, car, ParkCarType.black);
    }
}
