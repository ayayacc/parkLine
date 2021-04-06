package com.kl.parkLine;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.kl.parkLine.dao.IOrderDao;
import com.kl.parkLine.dao.IRoleDao;
import com.kl.parkLine.entity.Car;
import com.kl.parkLine.entity.Device;
import com.kl.parkLine.entity.Order;
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
import com.kl.parkLine.enums.OrderStatus;
import com.kl.parkLine.enums.OrderType;
import com.kl.parkLine.enums.ParkCarType;
import com.kl.parkLine.enums.PaymentType;
import com.kl.parkLine.enums.PlaceType;
import com.kl.parkLine.enums.PlateColor;
import com.kl.parkLine.enums.RoleType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.MonthlyTktParam;
import com.kl.parkLine.service.CarService;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.service.ParkService;
import com.kl.parkLine.service.UserService;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.OrderVo;

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
    
    @Autowired
    private OrderService orderservice;
    
    @Autowired
    private IOrderDao orderDao;
    
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
        //initCar();
        initMonthlyTkt();
        //initPark();
        //initRole();
        //initUser();
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
            park.setAddress(parkStr[1]+"address");
            park.setTotalTmpCnt(Integer.valueOf(parkStr[2]));
            Geometry point = wktReader.read(parkStr[3]);
            Point pointToSave = point.getInteriorPoint();
            park.setGeo(pointToSave);
            park.setContact(parkStr[4]);
            park.setTotalTmpCnt(100);
            park.setAvailableTmpCnt(100);
            park.setTotalUndergroundMonthlyCnt(100);
            park.setAvailableUndergroundMonthlyCnt(100);
            park.setTotalGroundMonthlyCnt(100);
            park.setAvailableGroundMonthlyCnt(100);
            park.setFuelGroundMonthlyPrice(new BigDecimal(300)); //月票300元
            park.setNewEnergyGroundMonthlyPrice(new BigDecimal(280)); //月票280元
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
                {"桂B69H60", "blue"},
                {"桂B539H0", "blue"},
                {"桂BDA3630", "green"},
                {"桂BZ7900", "blue"},
                {"桂BCM800", "blue"},
                {"桂BPC350", "blue"},
                {"桂GCN531", "blue"},
                {"桂BH7821", "blue"},
                {"桂B7N351", "blue"},
                {"桂B213U1", "blue"},
                {"桂B038U2", "blue"},
                {"桂BWW892", "blue"},
                {"桂AS9262", "blue"},
                {"桂BCC962", "blue"},
                {"桂BX6922", "blue"},
                {"桂B15D12", "blue"},
                {"桂B1093B", "blue"},
                {"桂B7v063", "blue"},
                {"桂B01R13", "blue"},
                {"桂BD01833", "green"},
                {"桂BDC9993", "green"},
                {"桂BD26683", "green"},
                {"桂BTF134", "blue"},
                {"桂B78F54", "blue"},
                {"桂BLP265", "blue"},
                {"桂B73885", "blue"},
                {"桂BQR556", "blue"},
                {"桂B8P206", "blue"},
                {"桂BXV166", "blue"},
                {"桂BS8576", "blue"},
                {"桂AQ776V", "blue"},
                {"桂BN7696", "blue"},
                {"桂BDR526", "green"},
                {"桂B7V586", "blue"},
                {"桂BQ1127", "blue"},
                {"桂B990Z7", "blue"},
                {"桂BXV167", "blue"},
                {"桂B8877A", "blue"},
                {"桂BDC1768", "green"},
                {"桂GXH908", "blue"},
                {"桂BXP078", "blue"},
                {"桂BVA108", "blue"},
                {"桂B65638", "blue"},
                {"桂B8Q418", "blue"},
                {"桂B26068", "blue"},
                {"桂BTM399", "blue"},
                {"桂BZD239", "blue"},
                {"桂BEJ899", "blue"},
                {"桂BD91289", "green"},
                {"桂BYK959", "blue"},
                {"桂BHK109", "blue"},
                {"桂BNV739", "blue"}
        };
        
        for (String[] carStr : carStrs)
        {
            carService.getCar(carStr[0], PlateColor.valueOf(carStr[1]));
        }
    }
    
    private void initMonthlyTkt() throws BusinessException, ParseException
    {
        String[][] monthlyTktParams = {
            {"桂BZ3683", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"粤SCH015", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B0261A", "blue", "underground", "2021-03-21", "2021-04-30", "200"},
            {"桂B9J316", "blue", "underground", "2021-03-13", "2021-12-31", "200"},
            {"桂B373Z9", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂B16M97", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂B7S851", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"粤T1172S", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂C972C5", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BWE655", "blue", "ground", "2021-03-01", "2021-04-30", "150"},
            {"桂BD24943", "green", "underground", "2021-03-01", "2021-03-31", "180"},
            {"桂BFS776", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B39T08", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BJ5985", "blue", "ground", "2021-04-01", "2021-04-30", "200"},
            {"桂B2672D", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BCZ521", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B810A8", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂BV8616", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B9M518", "blue", "underground", "2021-02-19", "2021-06-30", "200"},
            {"桂BGW689", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂BXZ987", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BNL287", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B8399A", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B87446", "blue", "underground", "2021-01-30", "2021-07-31", "200"},
            {"桂 BNN599", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BF16638", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BPY519", "blue", "underground", "2021-01-01", "2021-02-28", "200"},
            {"桂BXC206", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BDF2720", "green", "underground", "2021-03-01", "2021-02-28", "180"},
            {"渝A780XS", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BF20209", "blue", "underground", "2021-01-18", "2021-03-31", "200"},
            {"桂B0Q126", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BVX282", "blue", "underground", "2021-03-03", "2021-03-31", "200"},
            {"渝A780X5", "blue", "ground", "2021-03-01", "2021-02-28", "200"},
            {"湘A5JQ62", "blue", "ground", "2021-03-01", "2021-02-08", "150"},
            {"桂BDF7703", "green", "underground", "2021-03-01", "2021-04-30", "180"},
            {"桂B855Y1", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B5S162", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BGT177", "green", "ground", "2021-02-01", "2021-02-28", "135"},
            {"桂BA6830", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BHT963", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BHX983", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B017U2", "blue", "underground", "2021-03-01", "2021-03-22", "200"},
            {"桂BX8095", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BFQ838", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B819Q0", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂A441EZ", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"粤SG0H15", "blue", "underground", "2021-02-17", "2021-03-31", "200"},
            {"桂BLM122", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"桂B678B8", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B083U8", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B83L10", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂B9S617", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BGF088", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B639V8", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B798P9", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"桂BLT913", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BUF077", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂A7037D", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BNU560", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"闵CU791A", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂BG7322", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B2W122", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂BYQ448", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BDC3937", "green", "underground", "2021-03-01", "2021-04-30", "180"},
            {"桂B8D666", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂B2A003", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B84x00", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BHG211", "blue", "ground", "2021-02-01", "2021-02-28", "150"},
            {"桂BD54222", "green", "underground", "2021-02-01", "2021-03-31", "180"},
            {"桂AC9P12", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂ATY177", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BDE3335", "green", "underground", "2021-02-22", "2021-03-31", "180"},
            {"桂BD85955", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BJJ502", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂 BH5002", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BD70575", "green", "underground", "2021-02-11", "2021-03-31", "180"},
            {"桂B027W7", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"桂BC0691", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B26522", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BD8090", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂B767L2", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BD9323", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BSL238", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BTX293", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B08B06", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B711U5", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B853Y1", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B205Q0", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B618R9", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂BAJ010", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BM0996", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BPX906", "blue", "ground", "2021-02-01", "2021-02-28", "150"},
            {"桂B8Z921", "blue", "ground", "2020-02-01", "2020-11-30", "150"},
            {"沪C501HG", "blue", "underground", "2021-01-18", "2021-02-28", "200"},
            {"桂B9T005", "blue", "ground", "2021-01-01", "2021-06-30", "200"},
            {"桂BDC6273", "green", "ground", "2021-01-01", "2021-06-30", "180"},
            {"桂BD87219", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"粤SZ91M6", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B181P2", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B880Z3", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B73200", "blue", "underground", "2021-01-01", "2021-02-28", "200"},
            {"桂B12U30", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BEQ780", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BZK565", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BH6547", "blue", "underground", "2020-09-01", "2020-12-31", "200"},
            {"桂BFQ705", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BLL660", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂B32J22", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BNP275", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BVC908", "blue", "ground", "2021-01-01", "2021-01-31", "200"},
            {"桂B837L7", "blue", "ground", "2021-04-01", "2021-04-30", "200"},
            {"桂BDC2137", "blue", "ground", "2021-02-01", "2021-12-31", "200"},
            {"桂BD82398", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂B2G911", "blue", "underground", "2020-08-24", "2020-10-31", "200"},
            {"桂BMH727", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BF1650", "blue", "underground", "2021-02-01", "2021-04-30", "200"},
            {"桂BSZ191", "blue", "underground", "2021-01-09", "2021-06-30", "200"},
            {"桂B96N02", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BD98173", "green", "underground", "2021-01-01", "2021-01-31", "180"},
            {"桂B792Y7", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BER212", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BJU728", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B771L3", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BKA329", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BDA1120", "green", "underground", "2021-03-01", "2021-04-30", "180"},
            {"桂BS0202", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B4T128", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B25H09", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"渝B100L8", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B99P95", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂B370Z7", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"浙DR95Y6", "blue", "ground", "2020-02-01", "2020-10-31", "200"},
            {"桂BMC725", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂B113A3", "blue", "ground", "2020-02-01", "2020-10-31", "200"},
            {"桂B528W2", "blue", "ground", "2020-02-01", "2020-11-30", "200"},
            {"桂B55517", "blue", "ground", "2020-08-01", "2020-08-31", "150"},
            {"桂B107G9", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BPZ239", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BQY751", "blue", "ground", "2021-01-01", "2022-02-28", "200"},
            {"桂BBP227", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂B83M18", "blue", "ground", "2021-02-01", "2021-02-28", "200"},
            {"桂BD39698", "green", "ground", "2021-03-01", "2021-04-30", "180"},
            {"桂B01J27", "blue", "ground", "2021-03-01", "2021-06-30", "200"},
            {"桂B58W83", "blue", "underground", "2021-02-01", "2021-05-31", "200"},
            {"桂B108J5", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂B31B02", "blue", "ground", "2020-02-01", "2020-08-31", "200"},
            {"桂B01H29", "blue", "ground", "2020-02-01", "2020-10-31", "200"},
            {"桂B8706A", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BBF207", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BD81276", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂B87T70", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂B560V0", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂B1V786", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BCT326", "blue", "ground", "2021-03-27", "2021-03-31", "150"},
            {"桂B50700", "blue", "ground", "2020-07-01", "2020-08-31", "200"},
            {"桂BTF956", "blue", "ground", "2021-02-01", "2021-12-31", "200"},
            {"桂B1W997", "blue", "ground", "2021-04-01", "2021-06-30", "200"},
            {"桂BNS697", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BTT880", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BNX157", "blue", "ground", "2021-03-01", "2021-05-31", "200"},
            {"桂B1Y125", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂BYF185", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BET328", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BJ0035", "blue", "ground", "2020-06-04", "2020-06-18", "200"},
            {"桂BEP361", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂BRH161", "blue", "ground", "2020-06-04", "2020-07-31", "200"},
            {"桂BJJ951", "blue", "ground", "2021-01-22", "2021-03-31", "200"},
            {"桂BA1782", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂B1F947", "blue", "ground", "2020-02-01", "2020-11-30", "200"},
            {"桂B9V182", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BBH961", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂BXU177", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂B8E707", "blue", "ground", "2021-01-01", "2021-03-31", "200"},
            {"桂BA9373", "blue", "ground", "2020-02-01", "2020-10-31", "150"},
            {"桂BDV900", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BMV782", "blue", "ground", "2021-02-01", "2021-03-31", "200"},
            {"桂BCY557", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂B339M8", "blue", "ground", "2020-06-01", "2020-12-31", "200"},
            {"桂B15D39", "blue", "ground", "2021-03-01", "2021-06-30", "200"},
            {"桂B3H318", "blue", "ground", "2021-02-01", "2021-02-28", "200"},
            {"桂B07F25", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂BFZ393", "blue", "ground", "2021-03-01", "2021-04-30", "150"},
            {"桂BZA675", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂MS9989", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"桂BAU239", "blue", "ground", "2021-03-01", "2021-05-31", "200"},
            {"桂BLB860", "blue", "ground", "2021-02-01", "2021-02-28", "200"},
            {"桂BDZ888", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂B296P1", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BD75090", "green", "ground", "2021-03-01", "2021-03-31", "135"},
            {"桂BWG561", "blue", "ground", "2020-12-01", "2021-01-08", "135"},
            {"桂BD05221", "green", "ground", "2021-03-01", "2021-03-31", "135"},
            {"桂B989P7", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BS2931", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BB5533", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BSF263", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BY6388", "blue", "ground", "2020-08-01", "2020-08-31", "150"},
            {"桂BSF551", "blue", "ground", "2020-11-01", "2020-11-07", "150"},
            {"桂BGQ506", "blue", "ground", "2021-03-01", "2021-06-30", "200"},
            {"桂BT6707", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BB8257", "blue", "ground", "2021-03-01", "2021-05-31", "200"},
            {"桂BY8947", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂B23Q93", "blue", "ground", "2021-02-01", "2021-02-28", "200"},
            {"粤E62Q11", "blue", "ground", "2021-03-01", "2021-03-31", "150"},
            {"桂BFY936", "blue", "ground", "2021-01-01", "2021-06-30", "200"},
            {"桂BVM581", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂B74F74", "blue", "ground", "2021-02-01", "2021-03-31", "150"},
            {"桂B25653", "blue", "ground", "2021-02-01", "2021-03-31", "150"},
            {"桂B02917", "blue", "underground", "2020-06-01", "2020-06-30", "200"},
            {"桂BJ9857", "blue", "ground", "2020-02-01", "2020-07-31", "200"},
            {"桂BXS995", "blue", "underground", "2020-02-01", "2020-09-30", "200"},
            {"桂BRZ199", "blue", "ground", "2020-06-01", "2020-06-30", "150"},
            {"桂B7N789", "blue", "underground", "2021-03-15", "2021-04-30", "150"},
            {"桂B26U31", "blue", "ground", "2021-04-01", "2021-04-30", "200"},
            {"桂BUT897", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B11120", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B368Q3", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BTZ592", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BAZ912", "blue", "underground", "2021-02-01", "2021-04-30", "200"},
            {"桂BYM802", "blue", "underground", "2021-02-01", "2021-07-31", "200"},
            {"桂B028L1", "blue", "ground", "2021-03-01", "2021-09-30", "200"},
            {"桂BVC766", "blue", "ground", "2021-02-01", "2021-12-31", "200"},
            {"桂BBE711", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B558A2", "blue", "underground", "2020-02-01", "2020-07-16", "200"},
            {"桂BV4886", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B128J8", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂B15E85", "blue", "underground", "2021-02-01", "2021-10-31", "200"},
            {"桂BC2303", "blue", "underground", "2021-03-22", "2021-03-31", "200"},
            {"桂B822M0", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B927C8", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂B31256", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BN6923", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂BTA318", "blue", "underground", "2021-01-01", "2021-06-30", "200"},
            {"桂B01R22", "blue", "underground", "2020-02-01", "2020-05-31", "200"},
            {"桂BC3921", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂B02J14", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"鄂DUN595", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂B51H60", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BW1865", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂BVJ753", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂A831SY", "blue", "underground", "2021-04-01", "2021-04-30", "200"},
            {"桂B15695", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂BUU127", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B8C008", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂B822W8", "blue", "underground", "2021-02-01", "2021-04-30", "200"},
            {"桂B9W951", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BBG733", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B21691", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BK9994", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂B0S657", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"粤E519NC", "blue", "ground", "2020-02-01", "2020-03-31", "200"},
            {"桂B261A7", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂BRW839", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BTU898", "blue", "underground", "2020-02-01", "2020-07-31", "200"},
            {"桂BG8485", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂BY1112", "blue", "ground", "2020-02-01", "2020-12-31", "200"},
            {"桂BYY764", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂B0T809", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B851J8", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B2E698", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂B7U121", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂B73578", "blue", "underground", "2021-01-01", "2021-05-31", "200"},
            {"桂BD71796", "green", "underground", "2021-03-01", "2021-03-31", "180"},
            {"桂BP2052", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BTA510", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BXL706", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"粤SN083Z", "blue", "underground", "2020-02-01", "2020-05-03", "200"},
            {"桂A9415D", "blue", "ground", "2020-02-01", "2020-05-31", "200"},
            {"桂B982T1", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BE3296", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"桂BPA141", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BLG261", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B88C26", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B60832", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BR7095", "blue", "ground", "2020-02-01", "2020-02-29", "200"},
            {"桂B4H889", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BN7191", "blue", "underground", "2020-02-01", "2020-02-29", "200"},
            {"桂B08520", "blue", "underground", "2020-02-01", "2020-08-31", "200"},
            {"桂BJL070", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BCP228", "blue", "underground", "2020-01-01", "2020-01-31", "200"},
            {"桂B8L783", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B3C928", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B79J51", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B98J99", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BNZ989", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BJF844", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂B72085", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂ABB077", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂B578B0", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂B23D77", "blue", "ground", "2020-02-01", "2020-03-31", "200"},
            {"桂BD22254", "green", "underground", "2021-02-01", "2021-06-30", "180"},
            {"桂BG3662", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂BYZ588", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"苏E677BH", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"湘H0LE19", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BLE560", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BQM775", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B6T816", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BAW101", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂BK3571", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B9F206", "blue", "underground", "2020-01-01", "2020-01-31", "200"},
            {"桂B7G886", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BUU848", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BZH969", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BNE059", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂BLK735", "blue", "ground", "2020-02-01", "2020-12-31", "200"},
            {"桂B10J98", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂BCG030", "blue", "ground", "2020-06-01", "2020-06-30", "200"},
            {"桂BY0925", "blue", "ground", "2020-01-01", "2020-01-31", "200"},
            {"桂BTN152", "blue", "ground", "2020-02-01", "2020-12-31", "200"},
            {"桂BFV185", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂BX7181", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂BK8932", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂B00G17", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂CNT381", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂B260B0", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BXT010", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂B12M08", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂GFN100", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂BYV959", "blue", "ground", "2020-02-01", "2020-02-29", "200"},
            {"桂BD41780", "green", "ground", "2021-03-01", "2021-06-30", "180"},
            {"桂BLS808", "blue", "underground", "2020-02-01", "2020-09-30", "200"},
            {"桂B7Z275", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B35B89", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B5T291", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂BUV513", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BKF700", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BDF982", "blue", "underground", "2021-01-01", "2021-01-31", "200"},
            {"桂B44D66", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B29490", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂BJ1373", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BFZ626", "blue", "underground", "2021-03-19", "2021-04-30", "200"},
            {"桂BW6520", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B517L7", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BQZ988", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BBN077", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂BJP997", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BUR130", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BPG785", "blue", "ground", "2021-02-01", "2022-02-22", "200"},
            {"桂BB2831", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂B0S826", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂BGR235", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BWZ799", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BFN926", "blue", "underground", "2021-01-01", "2021-06-30", "200"},
            {"桂BWX276", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BML377", "blue", "underground", "2021-01-01", "2021-12-31", "200"},
            {"桂BD24252", "green", "ground", "2021-03-01", "2021-03-31", "180"},
            {"桂BAF309", "blue", "underground", "2021-03-01", "2021-12-31", "200"},
            {"桂BQF183", "blue", "underground", "2021-02-01", "2021-03-31", "300"},
            {"桂BZB328", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B3D932", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂BLV122", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B2E868", "blue", "underground", "2021-04-01", "2021-05-31", "200"},
            {"桂BXF255", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BT7956", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂B6A230", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B505Z8", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂BB9975", "blue", "underground", "2021-01-01", "2021-06-30", "200"},
            {"桂B532H6", "blue", "underground", "2021-03-01", "2022-02-28", "200"},
            {"桂BB4516", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂BD20531", "green", "underground", "2021-03-01", "2021-04-30", "180"},
            {"桂BWM062", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B53F53", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂B6P950", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂B360B5", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂B317C7", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂BXH357", "blue", "underground", "2021-03-01", "2021-06-30", "200"},
            {"桂B08L81", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂BM8135", "blue", "ground", "2020-02-01", "2020-02-29", "200"},
            {"桂B08N38", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂B25C27", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BNB353", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂BML075", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B90J96", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂C50F77", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂PZ8082", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"川A1UU35", "blue", "underground", "2021-04-01", "2021-04-30", "200"},
            {"桂B012G9", "blue", "ground", "2021-02-01", "2021-06-30", "200"},
            {"桂B0D517", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂BCT272", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂BG7577", "blue", "ground", "2021-03-01", "2021-04-18", "200"},
            {"桂BJJ199", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂B35538", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"陕U68321", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BCM722", "blue", "ground", "2021-02-01", "2021-03-31", "200"},
            {"桂BGT318", "blue", "underground", "2021-04-01", "2021-06-30", "200"},
            {"桂BJQ682", "blue", "underground", "2021-02-01", "2021-05-31", "200"},
            {"桂B19R91", "blue", "underground", "2020-02-01", "2020-12-31", "200"},
            {"桂MBW861", "blue", "ground", "2021-03-01", "2021-03-31", "200"},
            {"桂BA0670", "blue", "underground", "2021-01-01", "2021-05-31", "200"},
            {"桂B29G33", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂B639T8", "blue", "underground", "2021-03-22", "2021-03-31", "200"},
            {"桂BME420", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B90B07", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BAV219", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B5E520", "blue", "ground", "2020-02-01", "2020-04-10", "200"},
            {"桂B7U711", "blue", "underground", "2020-02-01", "2020-04-30", "200"},
            {"桂B04E68", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂B533E1", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BZS673", "blue", "underground", "2021-02-01", "2021-02-28", "200"},
            {"桂BJJ750", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BZC787", "blue", "underground", "2020-06-01", "2020-06-30", "200"},
            {"桂BD20875", "green", "ground", "2021-03-01", "2021-03-31", "180"},
            {"桂BDE515", "blue", "underground", "2021-03-01", "2021-05-31", "200"},
            {"桂B50C08", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B285R7", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂BPT966", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂BXF616", "blue", "underground", "2021-01-01", "2021-03-31", "200"},
            {"桂BQS959", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂B5E618", "blue", "underground", "2021-03-01", "2021-03-31", "200"},
            {"桂BXU576", "blue", "underground", "2020-02-01", "2020-03-31", "200"},
            {"桂BER777", "blue", "ground", "2021-03-01", "2021-04-30", "200"},
            {"桂BC0606", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂BWP960", "blue", "underground", "2021-03-01", "2021-03-11", "200"},
            {"桂NZ3515", "blue", "underground", "2020-02-01", "2020-02-29", "200"},
            {"桂BF22719", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂BVM180", "blue", "ground", "2020-02-01", "2020-02-22", "200"},
            {"桂B678S9", "blue", "ground", "2020-02-01", "2020-02-29", "200"},
            {"桂B16A10", "blue", "underground", "2021-04-01", "2021-04-30", "200"},
            {"桂B127R8", "blue", "ground", "2021-02-01", "2021-07-31", "200"},
            {"桂BMG339", "blue", "underground", "2020-02-01", "2020-05-31", "200"},
            {"桂BAZ782", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂BDA163", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂BPA620", "blue", "underground", "2021-02-01", "2021-06-30", "200"},
            {"桂B0Q851", "blue", "underground", "2020-02-01", "2020-06-30", "200"},
            {"桂B86D63", "blue", "underground", "2021-02-01", "2021-12-31", "200"},
            {"桂BFQ905", "blue", "underground", "2021-02-01", "2021-03-31", "200"},
            {"桂B89D79", "blue", "underground", "2021-03-01", "2021-04-30", "200"},
            {"桂BZ8979", "blue", "ground", "2020-02-01", "2020-06-30", "200"},
            {"桂BMV662", "blue", "underground", "2020-07-01", "2020-08-01", "200"},
            {"桂B38H56", "blue", "underground", "2021-01-01", "2021-06-30", "200"},
            {"桂B87F22", "blue", "underground", "2021-03-01", "2021-03-31", "200"}
        };
        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (String[] monthlyTktParam : monthlyTktParams)
        {
            Car car = carService.getCar(monthlyTktParam[0], PlateColor.valueOf(monthlyTktParam[1]));
            Park park = parkService.findOneById(5);
            Order order = orderDao.findTopByTypeAndCarAndParkOrderByEndDateDesc(OrderType.monthlyTicket, car, park);
            if (null == order)
            {
                MonthlyTktParam param = new MonthlyTktParam();
                param.setCarId(car.getCarId());
                param.setParkId(park.getParkId());
                param.setPlaceType(PlaceType.valueOf(monthlyTktParam[2]));
                param.setStartDate(simpleDateFormat.parse(monthlyTktParam[3]));
                param.setEndDate(simpleDateFormat.parse(monthlyTktParam[4]));
                OrderVo orderVo = orderservice.createMonthlyTkt(param, "none");
                order = orderservice.findOneByOrderId(orderVo.getOrderId());
            }
            else
            {
                order.setPlaceTye(PlaceType.valueOf(monthlyTktParam[2]));
                order.setStartDate(simpleDateFormat.parse(monthlyTktParam[3]));
                order.setEndDate(simpleDateFormat.parse(monthlyTktParam[4]));
            }
            
            BigDecimal monthlyPrice = new BigDecimal(monthlyTktParam[5]);
            order.setMonthlyPrice(monthlyPrice);
            order.setRealPayedAmt(monthlyPrice);
            order.setPayedAmt(monthlyPrice);
            order.setRealUnpayedAmt(BigDecimal.ZERO);
            if (order.getEndDate().after(new Date()))
            {
                order.setStatus(OrderStatus.payed);
            }
            else
            {
                order.setStatus(OrderStatus.expired);
            }
            orderservice.save(order);
        }
    }
    
    @Test
    @Transactional
    @Rollback(false)
    public void updateMontlyTkt() throws BusinessException
    {
        List<Order> parkings = orderDao.findByTypeAndUsedMonthlyTktIsNotNull(OrderType.parking);
        for (Order parking : parkings)
        {
            Order m = orderservice.findOneByOrderId(parking.getUsedMonthlyTkt().getOrderId());
            m.setCar(parking.getCar());
            orderservice.save(m);
        }
        
    }
}
