package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Device;

@Repository
public interface IDeviceDao extends JpaRepository<Device, Integer>
{
    public Device findOneBySerialNo(String serialNo);
}