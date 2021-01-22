package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.DrivingLicense;

@Repository
public interface IDrivingLicenseDao extends JpaRepository<DrivingLicense, Integer>
{
}