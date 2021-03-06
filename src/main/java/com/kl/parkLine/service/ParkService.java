package com.kl.parkLine.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.component.MapCmpt;
import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.entity.Park;
import com.kl.parkLine.entity.ParkLog;
import com.kl.parkLine.entity.ParkStepFee;
import com.kl.parkLine.enums.ChargeType;
import com.kl.parkLine.enums.PlaceType;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.BaseEnumJson;
import com.kl.parkLine.json.QqMapPoiItem;
import com.kl.parkLine.json.QqMapSearchResult;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.ParkLocationVo;

/**
 * @author chenc
 *
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ParkService
{
    private final static Logger logger = LoggerFactory.getLogger(ParkService.class);
    
    @Autowired
    private IParkDao parkDao;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private WKTReader wktReader;
    
    @Autowired
    private MapCmpt mapCmpt;
    
    /**
     * 根据停车场编码找到停车场对象
     * @param code 停车场编码
     */
    public Park findOneByCode(String code)
    {
        return parkDao.findOneByCode(code);
    }
    
    /**
     * 找指定位置附近的停车场
     * @param code 停车场编码
     * @throws ParseException 
     * @throws BusinessException 
     */
    public List<ParkLocationVo> findNearby(Point centerPoint, Double distanceKm) throws ParseException
    {
        //找到附近停车场
        List<Map<String, Object>> rows = parkDao.findNearby(centerPoint, distanceKm);
        
        List<ParkLocationVo> ret = new ArrayList<>();
        //遍历所有结果行
        for (Map<String, Object> row : rows)
        {
            Integer freeTimeMin = 0;
            Park park = this.findOneByCode(row.get("code").toString());
            if (park.getChargeType().equals(ChargeType.fixed))
            {
                freeTimeMin = park.getFuelFixedFee().getFreeTime();
            }
            else
            {
                for (ParkStepFee parkSetpFee : park.getFuelStepFees())
                {
                    if (parkSetpFee.getAmt().setScale(0).equals(BigDecimal.ZERO))
                    {
                        freeTimeMin = parkSetpFee.getEndMin();
                        break;
                    }
                }
            }
                
            //获取自运营停车场数据
            Geometry geometry = wktReader.read(row.get("geotext").toString());
            Point point = geometry.getInteriorPoint();
            ParkLocationVo nearByParkVo = ParkLocationVo.builder().parkId((Integer)row.get("park_id"))
                    .code(row.get("code").toString())
                    .name(row.get("name").toString())
                    .totalTmpCnt((Integer)row.get("total_tmp_cnt"))
                    .availableTmpCnt((Integer)row.get("available_tmp_cnt"))
                    .totalGroundMonthlyCnt((Integer)row.get("total_ground_monthly_cnt"))
                    .availableGroundMonthlyCnt((Integer)row.get("available_ground_monthly_cnt"))
                    .totalUndergroundMonthlyCnt((Integer)row.get("total_underground_monthly_cnt"))
                    .availableUndergroundMonthlyCnt((Integer)row.get("available_underground_monthly_cnt"))
                    .lng(point.getX())
                    .lat(point.getY())
                    .contact(row.get("contact").toString())
                    .distance((Double)row.get("dist"))
                    .freeTimeMin(freeTimeMin)
                    .address(row.get("address").toString())
                    .build();
            
            ret.add(nearByParkVo);
        }
        
        //调用腾讯地图查找其他停车场
        QqMapSearchResult mapSearchResult = mapCmpt.search(centerPoint, distanceKm);
        if (0 == mapSearchResult.getStatus())
        {
            for (QqMapPoiItem qqMapPoiItem : mapSearchResult.getData())
            {
                ParkLocationVo neerByParkVo = ParkLocationVo.builder()
                        .name(qqMapPoiItem.getTitle())
                        .lng(qqMapPoiItem.getLocation().getLng())
                        .lat(qqMapPoiItem.getLocation().getLat())
                        .contact(qqMapPoiItem.getTel())
                        .address(qqMapPoiItem.getAddress()
                                .replace(qqMapPoiItem.getAddInfo().getProvince(), "")
                                .replace(qqMapPoiItem.getAddInfo().getCity(), "")) //地址中去掉省市信息
                        .build();
                ret.add(neerByParkVo);
            }
        }
        else
        {
            logger.error(mapSearchResult.getMessage());
        }
        
        return ret;
    }
    
    public Park findOneById(Integer parkId) throws BusinessException
    {
        Optional<Park> park = parkDao.findById(parkId);
        if (false == park.isPresent())
        {
            throw new BusinessException(String.format("无效的停车场Id: %d", parkId));
        }
        return park.get();
    }
    
    /**
     * 保存停车场
     * @param park
     * @throws BusinessException 
     */
    public void save(Park park) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == park.getParkId()) //新增数据
        {
            park.setLogs(new ArrayList<ParkLog>());
        }
        else //编辑已有数据
        {
            //编辑停车场，//合并字段
            Optional<Park> parkDst = parkDao.findById(park.getParkId());
            
            if (false == parkDst.isPresent())
            {
                throw new BusinessException(String.format("无效的停车场 Id: %d", park.getParkId()));
            }
            
            //记录不同点
            diff = util.difference(parkDst.get(), park);
            
            BeanUtils.copyProperties(park, parkDst.get(), util.getNullPropertyNames(park));
            
            park = parkDst.get();
        }
        
        //保存数据
        ParkLog log = new ParkLog();
        log.setDiff(diff);
        log.setRemark(park.getChangeRemark());
        if (!StringUtils.isEmpty(diff)  //至少有一项内容时才添加日志
            || !StringUtils.isEmpty(park.getChangeRemark()))
        {
            log.setPark(park);
            park.getLogs().add(log);
        }
        parkDao.save(park);
    }
    
    /**
     * 获取停车场支持的车位类型
     * @param park
     * @return
     */
    public List<BaseEnumJson> getPlaceTypes(Park park)
    {
        List<BaseEnumJson> placeTypes = new ArrayList<>();
        if (park.getHasGroundPlace() && 0 < park.getAvailableGroundMonthlyCnt())
        {
            placeTypes.add(BaseEnumJson.builder().value(PlaceType.ground.toString()).text(PlaceType.ground.getText()).build());
        }
        if (park.getHasUndergroundPlace() && 0 < park.getAvailableUndergroundMonthlyCnt())
        {
            placeTypes.add(BaseEnumJson.builder().value(PlaceType.underground.toString()).text(PlaceType.underground.getText()).build());
        }
        return placeTypes;
    }
    
    /**
     * 变化月租可用车位
     * @param order 引起变动的order
     * @param changeCnt 变化数量
     * @throws BusinessException 
     */
    public void changeMonthlyAvaliableCnt(Order order, int changeCnt) throws BusinessException
    {
        Park park = order.getPark();
        Integer currentCnt = 0;
        Integer newAvailableCnt = 0;
        if (order.getPlaceTye().equals(PlaceType.ground))
        {
            currentCnt = park.getAvailableGroundMonthlyCnt();
            newAvailableCnt = currentCnt + changeCnt;
            park.setAvailableGroundMonthlyCnt(newAvailableCnt);
        }
        else
        {
            currentCnt = park.getAvailableUndergroundMonthlyCnt();
            newAvailableCnt = currentCnt + changeCnt;
            park.setAvailableUndergroundMonthlyCnt(newAvailableCnt);
        }
        park.setChangeRemark(String.format("%s 月租可用车位变化: %d --> %d, 订单编号: %s", 
                order.getPlaceTye().getText(), currentCnt, newAvailableCnt, order.getCode()));
        this.save(park);
    }
}
