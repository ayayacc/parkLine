package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.entity.Park;

/**
 * @author chenc
 *
 */
@Service("parkService")
public class ParkService
{
    @Autowired
    private IParkDao parkDao;
    
    /**
     * 根据停车场编码找到停车场对象
     * @param code 停车场编码
     */
    @Transactional
    public Park findOneByCode(String code)
    {
        return parkDao.findOneByCode(code);
    }
}
