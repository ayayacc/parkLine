package com.kl.parkLine.predicate;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.entity.QInvoice;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.util.RoleCode;
import com.kl.parkLine.vo.InvoiceVo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

@Component
public class InvoicePredicates
{
    //可以访问说有优惠券的角色
    private final List<String> allAccessRoleCodes = new ArrayList<String>();
    
    public InvoicePredicates()
    {
        //系统管理员
        allAccessRoleCodes.add(RoleCode.SYS_ADMIN);
        //运营
        allAccessRoleCodes.add(RoleCode.BIZ_OPERATE);
        //财务
        allAccessRoleCodes.add(RoleCode.BIZ_FINANCIAL);
    }
    
    public Predicate fuzzy(InvoiceVo invoiceVo, User user) 
    {
        QInvoice qInvoice = QInvoice.invoice;
        BooleanBuilder where = new BooleanBuilder();
        
        //编号
        if (!StringUtils.isEmpty(invoiceVo.getCode()))
        {
            where.and(qInvoice.code.containsIgnoreCase(invoiceVo.getCode()));
        }
        
        //区分用户权限
        where.and(roleFilter(user));
        
        return where;
    }
    
    //角色过滤，普通用户只能看自己车辆
    private Predicate roleFilter(User user) 
    {
        QInvoice qInvoice = QInvoice.invoice;
        BooleanBuilder where = new BooleanBuilder();
        
        if (user.hasAnyRole(allAccessRoleCodes))
        {
            return where;
        }
        //其他用户只能看自己数据
        where.and(qInvoice.createdBy.eq(user.getName()));
        
        return where;
    }
}
