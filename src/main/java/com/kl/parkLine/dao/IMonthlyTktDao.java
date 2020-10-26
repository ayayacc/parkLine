package com.kl.parkLine.dao;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.MonthlyTkt;
import com.kl.parkLine.enums.MonthlyStatus;

@Repository
public interface IMonthlyTktDao extends JpaRepository<MonthlyTkt, Integer>
{
    public Boolean existsByCarCarNoAndParkParkIdAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            String carNo, Integer parkId, MonthlyStatus monthlyStatus, Date endDate, Date startDate);
}