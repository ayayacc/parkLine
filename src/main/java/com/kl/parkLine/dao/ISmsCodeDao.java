package com.kl.parkLine.dao;

import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kl.parkLine.entity.SmsCode;

@Repository(value="smsCodeDao")
public interface ISmsCodeDao extends JpaRepository<SmsCode, Integer>
{
    public SmsCode findTop1ByMobileAndEnabledOrderByCreatedDateDesc(String mobile, String enabled);
    public Set<SmsCode> findByMobileAndEnabled(String mobile, String enabled);
}