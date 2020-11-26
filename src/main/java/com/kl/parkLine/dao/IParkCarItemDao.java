package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkCarItem;
import com.kl.parkLine.enums.ParkCarType;

@Repository
public interface IParkCarItemDao extends JpaRepository<ParkCarItem, Integer>
{
    public boolean existsByParkAndCarAndParkCarType(Park park, Car car, ParkCarType parkCarType);
}