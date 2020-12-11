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

import com.kl.parkLine.entity.Role;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.service.RoleService;
import com.kl.parkLine.vo.RoleVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/roles", produces="application/json;charset=utf-8")
@Api(tags = "角色管理")
public class RoleController
{
    @Autowired
    private RoleService roleService;
    
    /**
     * 根据角色Id查询单个角色信息
     * @param roleId 角色Id
     * @param role
     * @return
     */
    @GetMapping(value = "/{roleId}")
    @PreAuthorize("hasPermission(#role, 'read')")
    @ApiOperation(value="查询角色", notes="根据角色Id查询单个角色信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header"),
        @ApiImplicitParam(name="roleId", value="角色Id", required=true, paramType="path")
    })
    public RestResult<RoleVo> getRole(@ApiParam(name="角色Id",type="path") @PathVariable("roleId") Integer roleId, 
            @ApiIgnore @PathVariable("roleId") Role role)
    {
        if (null == role)
        {
            return RestResult.failed(String.format("无效的角色Id: %d", roleId));
        }
        else
        {
            RoleVo roleVo = RoleVo.builder()
                    .roleId(role.getRoleId())
                    .name(role.getName())
                    .build();
            return RestResult.success(roleVo);
        }
    }
    
    /**
     * 分页查询角色列表
     * @param role 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录角色
     * @return 角色查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="查询角色清单", notes="分页查询角色清单")
    public RestResult<Page<RoleVo>> find(@ApiParam(name="查询条件",type="query")RoleVo roleVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(roleService.fuzzyFindPage(roleVo, pageable, auth.getName()));
    }
    
    /**
     * 新增/编辑角色
     * @param role 角色信息
     * @param remark 修改的备注
     * @return
     * @throws BusinessException
     */
    @PostMapping("/save")
    @ApiOperation(value="新增/编辑角色", notes="普通角色只能编辑自己的信息，管理员可以编辑所有角色，name，Id等系统生成的字段不能修改")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<RoleVo> save(@ApiParam(name="角色信息") @RequestBody Role role)
    {
        try
        {
            roleService.save(role);
            return RestResult.success();
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
}
