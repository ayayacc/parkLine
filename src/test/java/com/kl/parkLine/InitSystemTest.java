package com.kl.parkLine;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.dao.IRoleDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkCarItem;
import com.kl.parkLine.entity.ParkFixedFee;
import com.kl.parkLine.entity.ParkSpecialFee;
import com.kl.parkLine.entity.ParkStepFee;
import com.kl.parkLine.entity.Role;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.enums.CarType;
import com.kl.parkLine.enums.ChargeType;
import com.kl.parkLine.enums.DeviceUseage;
import com.kl.parkLine.enums.ParkCarType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.enums.RoleType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.service.ParkService;
import com.kl.parkLine.service.UserService;
import com.kl.parkLine.util.RoleCode;

@SpringBootTest
public class InitSystemTest
{
    @Autowired 
    protected MockAuditorAwareTest mockAuditorAware; 
    
    @Autowired
    private ParkService parkService;
    
    @Autowired
    private IRoleDao roleDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CarService carService;
    
    @Autowired
    private WKTReader wktReader;
    
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
        initCar();
        initPark();
        initRole();
        initUser();
    }
    
    /**
     * 初始化停车场
     * @throws BusinessException 
     * @throws ParseException 
     * @throws org.locationtech.jts.io.ParseException 
     */
    private void initPark() throws BusinessException, ParseException, org.locationtech.jts.io.ParseException
    {
        String[][] parksStr = {
            {"FixedPark01", "parkNameFixed", "100", "POINT(108.281 22.9033)", "parkContact01"},
            {"FixedParkWithSpecial01", "parkNameFixedWithSpecial", "100", "POINT(108.281 22.9033)", "parkContact01"},
            {"StepPark01", "parkNameStep", "100", "POINT(109.441 24.3226)", "parkContact02"}
        };
        //0--60分钟免费,60--120分钟10元,120--180分钟20元,180--999999分钟30元
        Integer[][] stepFeesData = {
            {0, 60, 0},{60, 120, 10},{120, 180, 20},{180, 999999, 30}
        };
        
        //18:00--19:00,每30分钟15元; 20:00--21:00,每15分钟18元
        String[][] specialFeeData = {
            {"18:00", "19:00", "30", "15"},{"20:00", "21:00", "15", "18"}
        };
        List<ParkSpecialFee> fuelSpecialFees = new ArrayList<>();
        List<ParkSpecialFee> newEnergySpecialFees = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (String[] specialFeeStr : specialFeeData)
        {
            //燃油车
            ParkSpecialFee fuelSpecialFee = new ParkSpecialFee();
            fuelSpecialFee.setCarType(CarType.fuel);
            fuelSpecialFee.setStartTime(sdf.parse(specialFeeStr[0]));
            fuelSpecialFee.setEndTime(sdf.parse(specialFeeStr[1]));
            fuelSpecialFee.setFeePeriod(Integer.valueOf(specialFeeStr[2]));
            fuelSpecialFee.setPrice(new BigDecimal(specialFeeStr[3]));
            fuelSpecialFees.add(fuelSpecialFee);
            
            //新能源车,费用打8折扣
            ParkSpecialFee newEnergySpecialFee = new ParkSpecialFee();
            newEnergySpecialFee.setCarType(CarType.newEnergy);
            newEnergySpecialFee.setStartTime(sdf.parse(specialFeeStr[0]));
            newEnergySpecialFee.setEndTime(sdf.parse(specialFeeStr[1]));
            newEnergySpecialFee.setFeePeriod(Integer.valueOf(specialFeeStr[2]));
            newEnergySpecialFee.setPrice(new BigDecimal(specialFeeStr[3]).setScale(2).multiply(new BigDecimal(0.8)));
            newEnergySpecialFees.add(newEnergySpecialFee);
        }
        
        for (String[] parkStr : parksStr)
        {
            String code = parkStr[0];
            Park park = parkService.findOneByCode(code);
            if (null == park)
            {
                park = new Park();
            }
            park.setEnabled("Y");
            park.setCode(code);
            park.setName(parkStr[1]);
            park.setTotalCnt(Integer.valueOf(parkStr[2]));
            Geometry point = wktReader.read(parkStr[3]);
            Point pointToSave = point.getInteriorPoint();
            park.setGeo(pointToSave);
            park.setContact(parkStr[4]);
            park.setTotalCnt(100);
            park.setAvailableCnt(100);
            park.setFuelMonthlyPrice(new BigDecimal(300)); //月票300元
            park.setNewEnergyMonthlyPrice(new BigDecimal(280)); //月票280元
            //设备
            List<Device> devices = new ArrayList<>();
            //入场设备
            Device deviceIn = new Device();
            deviceIn.setPark(park);
            deviceIn.setUseage(DeviceUseage.in);
            deviceIn.setSerialNo(String.format("DeviceSn_%s_%s", park.getCode(), DeviceUseage.in.toString()));
            deviceIn.setName(String.format("DeviceName_%s_%s", park.getName(), DeviceUseage.in.toString()));
            devices.add(deviceIn);
            //出设备
            Device deviceOut = new Device();
            deviceOut.setPark(park);
            deviceOut.setUseage(DeviceUseage.out);
            deviceOut.setSerialNo(String.format("DeviceSn_%s_%s", park.getCode(), DeviceUseage.out.toString()));
            deviceOut.setName(String.format("DeviceName_%s_%s", park.getName(), DeviceUseage.out.toString()));
            devices.add(deviceOut);
            park.setDevices(devices);
            
            //固定计费
            if (park.getName().equalsIgnoreCase("parkNameFixed")
                   ||park.getName().equalsIgnoreCase("parkNameFixedWithSpecial"))
            {
                park.setChargeType(ChargeType.fixed);
                
                //燃油车
                ParkFixedFee fixedFee = new ParkFixedFee();
                park.setFuelFixedFee(fixedFee);
                //60分钟免费,每60分钟,收费5元,24小时内封顶30元
                fixedFee.setFreeTime(60); //60分钟免费
                fixedFee.setFeePeriod(60); //每60分钟
                fixedFee.setPrice(new BigDecimal(5)); //5元
                fixedFee.setMaxPeriod(24); //24小时内封顶
                fixedFee.setMaxAmt(new BigDecimal(30)); //30元
                
                
                //特殊计费时段
                if (park.getName().equalsIgnoreCase("parkNameFixedWithSpecial"))
                {
                    for (ParkSpecialFee specialFee : fuelSpecialFees)
                    {
                        specialFee.setPark(park);
                    }
                    park.setFuelSpecialFees(fuelSpecialFees);
                }
                
                
                //新能源车
                fixedFee = new ParkFixedFee();
                park.setNewEnergyFixedFee(fixedFee);
                //60分钟免费,每60分钟,收费3元,24小时内封顶20元
                fixedFee.setFreeTime(60); //60分钟免费
                fixedFee.setFeePeriod(60); //每60分钟
                fixedFee.setPrice(new BigDecimal(3)); //3元
                fixedFee.setMaxPeriod(24); //24小时内封顶
                fixedFee.setMaxAmt(new BigDecimal(20)); //20元
                
                //特殊计费时段
                if (park.getName().equalsIgnoreCase("parkNameFixedWithSpecial"))
                {
                    for (ParkSpecialFee specialFee : newEnergySpecialFees)
                    {
                        specialFee.setPark(park);
                    }
                    park.setNewEnergySpecialFees(newEnergySpecialFees);
                }
                
                //白名单
                List<ParkCarItem> whiteList = new ArrayList<>();
                park.setWhiteList(whiteList);
                ParkCarItem item = new ParkCarItem();
                Car carWhite = carService.getCar("桂BB1111", PlateColor.blue);
                item.setCar(carWhite);
                item.setPark(park);
                item.setParkCarType(ParkCarType.white);
                whiteList.add(item);
                
                //黑名单
                List<ParkCarItem> blackList = new ArrayList<>();
                park.setBlackList(blackList);
                item = new ParkCarItem();
                Car carBlack = carService.getCar("桂HB1111", PlateColor.blue);
                item.setCar(carBlack);
                item.setPark(park);
                item.setParkCarType(ParkCarType.black);
                blackList.add(item);
            }
            else if (park.getName().equalsIgnoreCase("parkNameStep")) //阶梯计费
            {
                park.setChargeType(ChargeType.step);
                
                List<ParkStepFee> fuleStepFees = new ArrayList<ParkStepFee>();
                List<ParkStepFee> newEnergyStepFees = new ArrayList<ParkStepFee>();
                //阶梯计费
                for (Integer[] stepFeeData : stepFeesData)
                {
                    ParkStepFee fuleStepFee = new ParkStepFee();
                    fuleStepFee.setPark(park);
                    ParkStepFee newEnergyStepFee = new ParkStepFee();
                    newEnergyStepFee.setPark(park);
                    fuleStepFee.setCarType(CarType.fuel);
                    newEnergyStepFee.setCarType(CarType.newEnergy);
                    fuleStepFee.setStartMin(stepFeeData[0]);
                    newEnergyStepFee.setStartMin(stepFeeData[0]);
                    fuleStepFee.setEndMin(stepFeeData[1]);
                    newEnergyStepFee.setEndMin(stepFeeData[1]);
                    fuleStepFee.setAmt(new BigDecimal(stepFeeData[2]));
                    newEnergyStepFee.setAmt(new BigDecimal(stepFeeData[2]).setScale(2).multiply(new BigDecimal(0.8)));
                    fuleStepFees.add(fuleStepFee);
                    newEnergyStepFees.add(newEnergyStepFee);
                }
                park.setFuelStepFees(fuleStepFees);
                park.setNewEnergyStepFees(newEnergyStepFees);
            }
            
            parkService.save(park);
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
            User user = userService.findByName(name);
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
                Park park = parkService.findOneByCode(parkCode);
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
            
            userService.save(user);
        }
    }
    
    private void initCar() throws BusinessException, ParseException
    {
        String[][] carStrs = {
                {"桂B11111", "blue"},
                {"桂B22222新", "green"},
                {"桂BH1111", "blue"},
                {"桂BB1111", "blue"}
        };
        
        for (String[] carStr : carStrs)
        {
            carService.getCar(carStr[0], PlateColor.valueOf(carStr[1]));
        }
    }
}
