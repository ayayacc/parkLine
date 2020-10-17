package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.MonthlyTkt;

@Repository
public interface IMonthlyTktDao extends JpaRepository<MonthlyTkt, Integer>
{
}