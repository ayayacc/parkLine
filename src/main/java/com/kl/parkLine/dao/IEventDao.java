package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Event;

@Repository
public interface IEventDao extends JpaRepository<Event, Integer>
{
    public Event findOneByGuidAndParkCode(String guid, String parkCode);
    
}