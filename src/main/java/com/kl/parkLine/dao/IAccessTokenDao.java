package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.AccessToken;

@Repository
public interface IAccessTokenDao extends JpaRepository<AccessToken, Integer>
{
    public AccessToken findTopByOrderByValidTimeDesc();
}