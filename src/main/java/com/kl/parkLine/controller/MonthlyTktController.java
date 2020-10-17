package com.kl.parkLine.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kl.parkLine.entity.MonthlyTkt;
import com.kl.parkLine.json.MonthlyTktParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.MonthlyTktService;
import com.kl.parkLine.vo.MonthlyTktVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/monthlyTkts")
@Api(tags="月票管理")
public class MonthlyTktController
{
    @Autowired
    private MonthlyTktService monthlyTktService;
    
    /**
     * 购买月票
     */
    @PostMapping("/monthlyTkt/create")
    @ApiOperation(value="购买月票", notes="如果指定车牌号在指定停车场已经有月票，则续费，否则新建；如果已经存在的月票处于待支付状态，则返回失败")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> create(@ApiParam(name="月票参数", required=true) @RequestBody MonthlyTktParam payMonthlyTktParam)
    {
        //TODO: 购买月票
        return null;
    }
    
    /**
     * 分页查询月票
     * @param monthlyTkt 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 月票查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="分页查询月票", notes="分页批量查询月票")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<MonthlyTktVo>> find(@ApiParam(name="查询条件",type="query")MonthlyTktVo monthlyTktVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(monthlyTktService.fuzzyFindPage(monthlyTktVo, pageable, auth.getName()));
    }
    
    /**
     * 查询月票明细
     * @param monthlyTktId 月票Id
     * @return 月票明细
     */
    @GetMapping(value = "/{monthlyTktId}")
    @PreAuthorize("hasPermission(#monthlyTkt, 'read')")
    @ApiOperation(value="查询月票明细", notes="根据月票Id")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<MonthlyTktVo> getMonthlyTkt(@ApiParam(name="月票Id",type="path") @PathVariable("monthlyTktId") Integer monthlyTktId, 
            @ApiIgnore @PathVariable("monthlyTktId") MonthlyTkt monthlyTkt)
    {
        if (null == monthlyTkt)
        {
            return RestResult.failed(String.format("无效的月票Id: %d", monthlyTktId));
        }
        else 
        {
            MonthlyTktVo monthlyTktVo = MonthlyTktVo.builder()
                    .code(monthlyTkt.getCode())
                    .carId(monthlyTkt.getCar().getCarId())
                    .carNo(monthlyTkt.getCar().getCarNo())
                    .parkId(monthlyTkt.getPark().getParkId())
                    .parkName(monthlyTkt.getPark().getName())
                    .status(monthlyTkt.getStatus().getText())
                    .startDate(monthlyTkt.getStartDate())
                    .endDate(monthlyTkt.getEndDate())
                    .build();
            return RestResult.success(monthlyTktVo);
        }
    }
}
