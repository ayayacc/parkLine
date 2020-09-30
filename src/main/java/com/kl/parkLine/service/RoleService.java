package com.kl.parkLine.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IRoleDao;
import com.kl.parkLine.entity.Role;

/**
 * @author chenc
 *
 */
@Service("roleService")
public class RoleService
{
    @Autowired
    private IRoleDao roleDao;
    
    /**
     * 保存用户
     * @param user
     */
    @Transactional
    public void save(Role role)
    {
        roleDao.save(role);
    }
    
    /**
     * 根据代码找到唯一角色
     * @param code 角色编码
     * @return 角色对象
     */
    @Transactional(readOnly = true)
    public Role findOneByCode(String code)
    {
        return roleDao.findOneByCode(code);
    }
}
