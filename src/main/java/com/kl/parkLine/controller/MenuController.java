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

import com.kl.parkLine.entity.Menu;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.MenuService;
import com.kl.parkLine.vo.MenuVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/menus", produces="application/json;charset=utf-8")
@Api(tags = "菜单管理")
public class MenuController
{
    @Autowired
    private MenuService menuService;
    
    /**
     * 根据菜单Id查询单个菜单信息
     * @param menuId 菜单Id
     * @param menu
     * @return
     * @throws BusinessException 
     */
    @GetMapping(value = "/{menuId}")
    @PreAuthorize("hasPermission(#menu, 'read')")
    @ApiOperation(value="查询菜单", notes="根据菜单Id查询单个菜单信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header"),
        @ApiImplicitParam(name="menuId", value="菜单Id", required=true, paramType="path")
    })
    public RestResult<MenuVo> getMenu(@ApiParam(name="菜单Id",type="path") @PathVariable("menuId") Integer menuId, 
            @ApiIgnore @PathVariable("menuId") Menu menu) throws BusinessException
    {
        if (null == menu)
        {
            throw new BusinessException(String.format("无效的菜单Id: %d", menuId));
        }
        else
        {
            MenuVo menuVo = MenuVo.builder()
                    .menuId(menu.getMenuId())
                    .name(menu.getName())
                    .build();
            return RestResult.success(menuVo);
        }
    }
    
    /**
     * 分页查询菜单列表
     * @param menu 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录菜单
     * @return 菜单查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="查询菜单清单", notes="分页查询菜单清单")
    public RestResult<Page<MenuVo>> find(@ApiParam(name="查询条件",type="query")MenuVo menuVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(menuService.fuzzyFindPage(menuVo, pageable, auth.getName()));
    }
    
    /**
     * 新增/编辑菜单
     * @param menu 菜单信息
     * @param remark 修改的备注
     * @return
     * @throws BusinessException
     */
    @PostMapping("/save")
    @ApiOperation(value="新增/编辑菜单", notes="普通菜单只能编辑自己的信息，管理员可以编辑所有菜单，name，Id等系统生成的字段不能修改")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<MenuVo> save(@ApiParam(name="菜单信息") @RequestBody Menu menu) throws BusinessException
    {
        menuService.save(menu);
        return RestResult.success();
    }
}
