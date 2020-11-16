package com.kl.parkLine.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.IRoleDao;
import com.kl.parkLine.entity.Menu;
import com.kl.parkLine.entity.QRole;
import com.kl.parkLine.entity.Role;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.RolePredicates;
import com.kl.parkLine.vo.RoleVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class RoleService
{
    @Autowired
    private IRoleDao roleDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RolePredicates rolePredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    @Autowired
    private Utils util;
    
    /**
     * 根据代码找到唯一角色
     * @param code 角色编码
     * @return 角色对象
     */
    public Role findOneByCode(String code)
    {
        return roleDao.findOneByCode(code);
    }
    
    /**
     * 根据roleCode获取角色列表
     * @param roleCode 角色编码
     * @return
     * @throws BusinessException 
     */
    public Set<Menu> getRoleMenus(String roleCode)
    {
        Role role = findOneByCode(roleCode);
        return role.getMenus();
    }
    
    /**
     * 模糊匹配角色
     * @param role  
     * @param pageable
     * @param auth
     * @return
     */
    public Page<RoleVo> fuzzyFindPage(RoleVo roleVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = rolePredicates.fuzzy(roleVo, user);
        
        QRole qRole = QRole.role;
        QueryResults<RoleVo> queryResults = jpaQueryFactory
                .select(Projections.constructor(RoleVo.class, 
                        qRole.roleId,
                        qRole.code,
                        qRole.name))
                .from(qRole)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        return new PageImpl<>(queryResults.getResults(), pageable, queryResults.getTotal());
    }
    
    /**
     * 保存一个角色
     * @param 被保存的角色
     * @throws BusinessException 
     */
    public void save(Role role) throws BusinessException
    {
        //编辑角色，//合并字段
        Optional<Role> roleDst = roleDao.findById(role.getRoleId());
        
        if (false == roleDst.isPresent())
        {
            throw new BusinessException(String.format("无效的角色 Id: %d", role.getRoleId()));
        }
        
        //记录不同点
        BeanUtils.copyProperties(role, roleDst.get(), util.getNullPropertyNames(role));
        
        role = roleDst.get();
        
        //保存数据
        roleDao.save(role);
    }
}
