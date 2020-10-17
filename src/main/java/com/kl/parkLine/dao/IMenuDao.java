package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Menu;

@Repository
public interface IMenuDao extends JpaRepository<Menu, Integer>
{
}