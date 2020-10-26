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
import com.kl.parkLine.json.CreateMonthlyTktParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.WxunifiedOrderResult;
import com.kl.parkLine.service.MonthlyTktService;
import com.kl.parkLine.service.OrderService;
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
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 购买月票
     */
    @PostMapping("/create")
    @ApiOperation(value="购买月票", notes="新建一张月票；")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<WxunifiedOrderResult> create(@ApiParam(name="月票参数", required=true) @RequestBody CreateMonthlyTktParam monthlyTktParam
            , Authentication auth)
    {
        try
        {
            return RestResult.success(orderService.createMonthlyTkt(monthlyTktParam, auth.getName()));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 续费月票
     */
    @PostMapping("/monthlyTkt/renew")
    @ApiOperation(value="续费月票", notes="对指定月票进行续费(时间连续)；则返回失败")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> renew(@ApiParam(name="月票参数", required=true) @RequestBody CreateMonthlyTktParam payMonthlyTktParam)
    {
        //TODO: 续费月票
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
