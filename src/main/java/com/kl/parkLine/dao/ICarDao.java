package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Car;

@Repository
public interface ICarDao extends JpaRepository<Car, Integer>
{
    public Car findOneByCarNo(String carNo);
}