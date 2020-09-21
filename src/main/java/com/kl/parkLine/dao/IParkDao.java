package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Park;

@Repository(value="parkDao")
public interface IParkDao extends JpaRepository<Park, Integer>
{
    public Park findOneByCode(String code);
}