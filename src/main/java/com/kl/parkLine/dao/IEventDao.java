package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Event;

@Repository(value="eventDao")
public interface IEventDao extends JpaRepository<Event, Integer>
{
    public Event findOneByGuidAndParkCode(String guid, String parkCode);
    
}