package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.Dict;

@Repository(value="dictDao")
public interface IDictDao extends JpaRepository<Dict, Integer>
{
    public Dict findOneByCodeAndEnabled(String code, String enabled);
}