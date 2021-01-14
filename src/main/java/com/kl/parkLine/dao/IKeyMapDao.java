package com.kl.parkLine.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.KeyMap;

@Repository
public interface IKeyMapDao extends JpaRepository<KeyMap, Integer>
{
    public KeyMap findOneByPublicKey(String publicKey);
}