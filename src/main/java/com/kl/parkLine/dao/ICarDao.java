package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Car;

@Repository(value="carDao")
public interface ICarDao extends JpaRepository<Car, Integer>
{
    public Car findOneByCarNo(String carNo);
}