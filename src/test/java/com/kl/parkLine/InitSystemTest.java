package com.kl.parkLine;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.dao.IRoleDao;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.Role;
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
    }
    
    /**
     * 初始化停车场
     */
    private void initPark()
    {
        String[][] parksStr = {
            {"parkCode01", "parkName01", "100", "108.2815 22.9033", "parkContact01"}
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
            {RoleCode.END_USER, "最终用户"},
            {RoleCode.PARK_GUARD, "停车场值班人员（巡检或岗亭）"},
            {RoleCode.PARK_MANAGER, "停车场经理"},
            {RoleCode.PARK_OWNER, "停车场业主"},
            {RoleCode.SYS_OPERATION, "系统运营"},
            {RoleCode.SYS_ADMIN, "系统管理员"}
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
            
            roleDao.save(role);
        }
    }
}
