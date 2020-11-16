package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Car;
import com.kl.parkLine.enums.PlateColor;

@Repository
public interface ICarDao extends JpaRepository<Car, Integer>
{
    public Car findOneByCarNoAndPlateColor(String carNo, PlateColor plateColor);
}