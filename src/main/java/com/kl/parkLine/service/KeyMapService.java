package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IKeyMapDao;
import com.kl.parkLine.entity.KeyMap;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class KeyMapService
{
    @Autowired
    private IKeyMapDao keyMapDao;
    
    /**
     * 根据公钥找到对象
     * @param publicKey
     */
    public KeyMap findOneByPublicKey(String publicKey)
    {
        return keyMapDao.findOneByPublicKey(publicKey);
    }
}
