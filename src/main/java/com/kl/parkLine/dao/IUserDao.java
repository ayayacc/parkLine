package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.User;

@Repository(value="userDao")
public interface IUserDao extends JpaRepository<User, Integer>
{
    public User findOneByName(String name);
    public User findOneByMobile(String mobile);
}