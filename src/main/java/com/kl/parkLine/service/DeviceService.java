package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IDeviceDao;
import com.kl.parkLine.entity.Device;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class DeviceService
{
    @Autowired
    private IDeviceDao deviceDao;

    /**
     * 根据停车场编码找到停车场对象
     * @param code 停车场编码
     */
    public Device findOneBySerialNo(String serialNo)
    {
        return deviceDao.findOneBySerialNo(serialNo);
    }
}
