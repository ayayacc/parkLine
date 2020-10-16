package com.kl.parkLine.controller;

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

import com.kl.parkLine.entity.Invoice;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.vo.InvoiceVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/invoices")
@Api(tags = "发票管理")
public class InvoiceController
{
    /**
     * 根据发票Id查询单个发票信息
     * @param invoiceId 发票Id
     * @param invoice
     * @return
     */
    @GetMapping(value = "/{invoiceId}")
    @PreAuthorize("hasPermission(#invoice, 'read')")
    @ApiOperation(value="查询发票", notes="根据发票Id查询单个发票信息")
    @ApiImplicitParams({
        @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header"),
        @ApiImplicitParam(name="invoiceId", value="发票Id", required=true, paramType="path")
    })
    public RestResult<InvoiceVo> getInvoice(@ApiParam(name="发票Id",type="path") @PathVariable("invoiceId") Integer invoiceId, 
            @ApiIgnore @PathVariable("invoiceId") Invoice invoice)
    {
        if (null == invoice)
        {
            return RestResult.failed(String.format("无效的发票Id: %d", invoiceId));
        }
        else
        {
            InvoiceVo invoiceVo = InvoiceVo.builder()
                    .invoiceId(invoice.getInvoiceId())
                    .code(invoice.getCode())
                    .amt(invoice.getAmt())
                    .status(invoice.getStatus().getText())
                    .build();
            return RestResult.success(invoiceVo);
        }
    }
    
    /**
     * 分页查询发票列表
     * @param invoice 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录发票
     * @return 发票查询结果
     */
    @GetMapping("/find")
    @ApiOperation(value="查询发票清单", notes="分页查询发票清单")
    public RestResult<Page<InvoiceVo>> find(@ApiParam(name="查询条件",type="query")Invoice invoice, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        //TODO: 分页查询发票
        return null;
    }
    
    /**
     * 新增/编辑发票
     * @param invoice 发票信息
     * @param remark 修改的备注
     * @return
     * @throws BusinessException
     */
    @PostMapping("/save")
    @ApiOperation(value="新增发票申请", notes="普通发票只能编辑自己的信息，管理员可以编辑所有发票，name，Id等系统生成的字段不能修改")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<InvoiceVo> apply(@ApiParam(name="发票信息") @RequestBody Invoice invoice)
    {
        try
        {
            //invoiceService.save(invoice);
            return RestResult.success();
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
}
