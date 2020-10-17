package com.kl.parkLine.predicate;

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
        //TODO: 进行数据隔离
        //普通用户只能看自己发票
        if (user.hasRole(RoleCode.END_USER))
        {
            where.and(qInvoice.createdBy.eq(user.getName()));
        }
        
        return where;
    }
}
