package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IOrderLogDao;
import com.kl.parkLine.entity.OrderLog;


/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class OrderLogService
{
    @Autowired
    private IOrderLogDao orderLogDao;
    
    /**
     * 保存订单日志
     * @param 保存订单日志
     */
    public void save(OrderLog orderLog)
    {
        orderLogDao.save(orderLog);
    }
}
