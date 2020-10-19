package com.kl.parkLine;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.dao.IRoleDao;
import com.kl.parkLine.dao.IUserDao;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.Role;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.RoleType;
import com.kl.parkLine.util.RoleCode;

@SpringBootTest
public class InitSystemTest
{
    @Autowired 
    protected MockAuditorAwareTest mockAuditorAware; 
    
    @Autowired
    private IParkDao parkDao;
    
    @Autowired
    private IRoleDao roleDao;
    
    @Autowired
    private IUserDao userDao;
    
    @BeforeEach
    public void wireUpAuditor()
    {
        mockAuditorAware.setCurrentAuditor("admin");
    }
    
    //初始化字典
    @Test
    @Transactional
    @Rollback(false)
    public void initSystem() throws Exception
    {
        initPark();
        initRole();
        initUser();
    }
    
    /**
     * 初始化停车场
     */
    private void initPark()
    {
        String[][] parksStr = {
            {"parkCode01", "parkName01", "100", "108.2815 22.9033", "parkContact01"},
            {"parkCode02", "parkName02", "100", "108.2815 22.9033", "parkContact02"}
        };
        
        for (String[] parkStr : parksStr)
        {
            String code = parkStr[0];
            Park park = parkDao.findOneByCode(code);
            if (null == park)
            {
                park = new Park();
            }
            park.setEnabled("Y");
            park.setCode(code);
            park.setName(parkStr[1]);
            park.setTotalCnt(Integer.valueOf(parkStr[2]));
            park.setGeo(parkStr[3]);
            park.setContact(parkStr[4]);
            park.setTotalCnt(100);
            park.setAvailableCnt(100);
            park.setFreeTime(60); //60分钟免费
            //超过60分钟，第一个小时5块
            park.setTimeLev1(60); 
            park.setPriceLev1(new BigDecimal(5));
            //每半小时3块
            park.setTimeLev2(30); 
            park.setPriceLev2(new BigDecimal(3));
            parkDao.save(park);
        }
    }
    
    /**
     * 初始化角色
     */
    private void initRole()
    {
        String[][] rolesStr = {
            {RoleCode.SYS_ADMIN, "系统管理员", "company"},
            {RoleCode.BIZ_MARKETING, "业务市场", "company"},
            {RoleCode.BIZ_OPERATE, "业务运营", "company"},
            {RoleCode.BIZ_CUSTOMERE_SERVICE, "客服", "company"},
            {RoleCode.BIZ_FINANCIAL, "财务", "company"},
            {RoleCode.BIZ_PATROL, "巡检", "company"},
            {RoleCode.PARK_ADMIN, "停车场管理员", "park"},
            {RoleCode.PARK_GUARD, "停车场值班人员（巡检或岗亭）", "park"},
            {RoleCode.PARK_FINANCIAL, "停车场财务", "park"},
            {RoleCode.END_USER, "终端用户", "endUser"}
        };
        
        for (String[] roleStr : rolesStr)
        {
            String code = roleStr[0];
            Role role = roleDao.findOneByCode(code);
            if (null == role)
            {
                role = new Role();
            }
            role.setCode(code);
            role.setName(roleStr[1]);
            role.setType(RoleType.valueOf(roleStr[2]));
            
            roleDao.save(role);
        }
    }
    
    private void initUser()
    {
        String[][] usersStr = {
                {"PARK_01_ADMIN", "parkUserMobile01", "parkCode01", "ROLE_PARK_ADMIN"},
                {"PARK_02_ADMIN", "parkUserMobile02", "parkCode02", "ROLE_PARK_ADMIN"},
                {"SYS_ADMIN", "sysAdminMobile01", "", "ROLE_SYS_ADMIN"}
            };
        for (String[] userStr : usersStr)
        {
            String name = userStr[0];
            User user = userDao.findOneByName(name);
            if (null == user)
            {
                user = new User();
            }
            user.setName(name);
            user.setMobile(userStr[1]);
            user.setEnabled(true);
            
            //所属停车场
            String parkCode = userStr[2];
            if (!StringUtils.isEmpty(parkCode))
            {
                Park park = parkDao.findOneByCode(parkCode);
                Set<Park> parks = new HashSet<>();
                parks.add(park);
                user.setParks(parks);
            }
            
            //角色
            String roleCode = userStr[3];
            if (!StringUtils.isEmpty(roleCode))
            {
                Role role = roleDao.findOneByCode(roleCode);
                Set<Role> roles = new HashSet<>();
                roles.add(role);
                user.setRoles(roles);
            }
            
            userDao.save(user);
        }
    }
}
