package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Role;

@Repository
public interface IRoleDao extends JpaRepository<Role, Integer>
{
    public Role findOneByCode(String code);
}