package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkStepFee;
import com.kl.parkLine.enums.CarType;

@Repository
public interface IParkStepFeeDao extends JpaRepository<ParkStepFee, Integer>
{
    public ParkStepFee findOneByParkAndCarTypeAndStartMinLessThanEqualAndEndMinGreaterThan(Park park, CarType carType, Integer lessThan, Integer GreaterThan);
}