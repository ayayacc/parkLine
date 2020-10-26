package com.kl.parkLine.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kl.parkLine.component.Utils;
import com.kl.parkLine.dao.IInvoiceDao;
import com.kl.parkLine.entity.Invoice;
import com.kl.parkLine.entity.InvoiceLog;
import com.kl.parkLine.entity.QInvoice;
import com.kl.parkLine.entity.User;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.predicate.InvoicePredicates;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.InvoiceVo;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQueryFactory;

/**
 * @author chenc
 *
 */
@Service
public class InvoiceService
{
    @Autowired
    private IInvoiceDao invoiceDao;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private Utils util;
    
    @Autowired
    private InvoicePredicates invoicePredicates;
    
    @Autowired
    private JPAQueryFactory jpaQueryFactory;
    
    /**
     * 保存一个优惠券定义
     * @param 被保存的优惠券
     * @throws BusinessException 
     */
    @Transactional
    public void save(Invoice invoice) throws BusinessException
    {
        String diff = Const.LOG_CREATE;
        if (null == invoice.getInvoiceId()) //新增数据
        {
            invoice.setLogs(new ArrayList<InvoiceLog>());
        }
        else//编辑已有数据
        {
            //编辑优惠券定义，//合并字段
            Optional<Invoice> invoiceDst = invoiceDao.findById(invoice.getInvoiceId());
            
            if (false == invoiceDst.isPresent())
            {
                throw new BusinessException(String.format("无效的优惠券定义 Id: %d", invoice.getInvoiceId()));
            }
            
            //记录不同点
            diff = util.difference(invoiceDst.get(), invoice);
            
            BeanUtils.copyProperties(invoice, invoiceDst.get(), util.getNullPropertyNames(invoice));
            
            invoice = invoiceDst.get();
        }
        
        //保存数据
        InvoiceLog log = new InvoiceLog();
        log.setDiff(diff);
        log.setRemark(invoice.getChangeRemark());
        log.setInvoice(invoice);
        invoice.getLogs().add(log);
        invoiceDao.save(invoice);
    }
    
    /**
     * 模糊匹配优惠券定义
     * @param invoice  
     * @param pageable
     * @param auth
     * @return
     */
    @Transactional(readOnly = true)
    public Page<InvoiceVo> fuzzyFindPage(InvoiceVo invoiceVo, Pageable pageable, String userName)
    {
        User user = userService.findByName(userName);
        Predicate searchPred = invoicePredicates.fuzzy(invoiceVo, user);
        
        QInvoice qInvoice = QInvoice.invoice;
        QueryResults<Tuple> queryResults = jpaQueryFactory
                .select(
                        qInvoice.invoiceId,
                        qInvoice.code,
                        qInvoice.amt,
                        qInvoice.status
                )
                .from(qInvoice)
                .where(searchPred)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();
        //转换成vo
        List<InvoiceVo> invoiceVos = queryResults
                .getResults()
                .stream()
                .map(tuple -> InvoiceVo.builder()
                        .invoiceId(tuple.get(qInvoice.invoiceId))
                        .code(tuple.get(qInvoice.code))
                        .amt(tuple.get(qInvoice.amt))
                        .status(tuple.get(qInvoice.status).getText())
                        .build())
                .collect(Collectors.toList());
        return new PageImpl<>(invoiceVos, pageable, queryResults.getTotal());
    }
}
