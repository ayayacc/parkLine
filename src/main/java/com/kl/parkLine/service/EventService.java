package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IEventDao;
import com.kl.parkLine.entity.Event;

/**
 * @author chenc
 *
 */
@Service("eventService")
public class EventService
{
    @Autowired
    private IEventDao eventDao;
    
    /**
     * 保存用户
     * @param user
     */
    @Transactional
    public void save(Event event)
    {
        eventDao.save(event);
        return;
    }
}
