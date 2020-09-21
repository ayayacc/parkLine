package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IDictDao;
import com.kl.parkLine.entity.Dict;

/**
 * @author chenc
 *
 */
@Service("dictService")
public class DictService
{
    @Autowired
    private IDictDao dictDao;
    
    /**
     * 保存用户
     * @param user
     */
    @Transactional(readOnly = true)
    public Dict findOneByCode(String code)
    {
        return dictDao.findOneByCodeAndEnabled(code, "Y");
    }
}
