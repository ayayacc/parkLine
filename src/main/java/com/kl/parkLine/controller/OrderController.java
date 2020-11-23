package com.kl.parkLine.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.wxpay.sdk.WXPayUtil;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.enums.DeviceUseage;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.ActiveCouponParam;
import com.kl.parkLine.json.CarParam;
import com.kl.parkLine.json.ChargeWalletParam;
import com.kl.parkLine.json.MonthlyTktParam;
import com.kl.parkLine.json.PayOrderParam;
import com.kl.parkLine.json.RestResult;
import com.kl.parkLine.json.WxPayNotifyParam;
import com.kl.parkLine.json.WxUnifiedOrderResult;
import com.kl.parkLine.service.OrderService;
import com.kl.parkLine.util.Const;
import com.kl.parkLine.vo.OrderVo;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping(value="/orders")
@Api(tags="订单管理")
public class OrderController
{
    @Autowired 
    private OrderService orderService;  
    
    @Autowired
    private HttpServletRequest request;
    
    /**
     * 查询月票价格
     */
    @PostMapping("/monthlyTkt/inquiry")
    @ApiOperation(value="查询月票价格", notes="根据停车场，起止时间，查询停车场月票价格")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> inquiryMonthlyTkt(@ApiParam(name="月票参数", required=true) @RequestBody(required=true) MonthlyTktParam monthlyTktParam, 
            Authentication auth)
    {
        try
        {
            return RestResult.success(orderService.inqueryMonthlyTkt(monthlyTktParam));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 购买月票
     */
    @PostMapping("/monthlyTkt/create")
    @ApiOperation(value="购买月票", notes="新建一张月票；")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<OrderVo> createMonthlyTkt(@ApiParam(name="月票参数", required=true) @RequestBody(required=true) MonthlyTktParam monthlyTktParam, 
            Authentication auth)
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
     * 购买月票
     */
    @PostMapping("/wallet/charge")
    @ApiOperation(value="钱包充值", notes="钱包充值")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<WxUnifiedOrderResult> chargeWallet(@ApiParam(name="充值参数", required=true) @RequestBody ChargeWalletParam walletChargeParam, Authentication auth)
    {
        try
        {
            return RestResult.success(orderService.createWalletChargeOrder(walletChargeParam, auth.getName()));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 激活优惠券
     */
    @PostMapping("/coupon/active")
    @ApiOperation(value="激活优惠券", notes="将优惠券的有效期延期一周")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> activeCoupon(@ApiParam(name="优惠券参数", required=true) @RequestBody ActiveCouponParam activeCouponParam, Authentication auth)
    {
        try
        {
            return RestResult.success(orderService.createActiveCouponOrder(activeCouponParam, auth.getName()));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 终端用户分页查询订单
     * @param order 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 订单查询结果
     */
    @GetMapping("/findAsUser")
    @ApiOperation(value="分页查询订单", notes="分页批量查询订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> findAsUser(@ApiParam(name="查询条件",type="query")OrderVo orderVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(orderService.fuzzyFindPageAsUser(orderVo, pageable, auth.getName()));
    }
    
    /**
     * 停车场/公司管理分页查询订单
     * @param order 查询条件
     * @param pageable 分页条件
     * @param auth 当前登录用户
     * @return 订单查询结果
     */
    @GetMapping("/findAsManager")
    @ApiOperation(value="分页查询订单", notes="分页批量查询订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> findAsManager(@ApiParam(name="查询条件",type="query")OrderVo orderVo, 
            @ApiParam(name="分页信息",type="query") Pageable pageable, Authentication auth)
    {
        return RestResult.success(orderService.fuzzyFindPageAsManager(orderVo, pageable, auth.getName()));
    }
    
    /**
     * 查询订单明细
     * @param orderId 订单Id
     * @return 订单明细
     */
    @GetMapping(value = "/{orderId}")
    @PreAuthorize("hasPermission(#order, 'read')")
    @ApiOperation(value="查询订单明细", notes="根据订单Id")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<OrderVo> getOrder(@ApiParam(name="订单Id",type="path") @PathVariable("orderId") Integer orderId, 
            @ApiIgnore @PathVariable("orderId") Order order)
    {
        if (null == order)
        {
            return RestResult.failed(String.format("无效的订单Id: %d", orderId));
        }
        else 
        {
            OrderVo orderVo = OrderVo.builder()
                    .code(order.getCode())
                    .orderId(order.getOrderId())
                    .status(order.getStatus())
                    .parkParkId(order.getPark().getParkId())
                    .parkName(order.getPark().getName())
                    .carCarNo(order.getCar().getCarNo())
                    .carCarId(order.getCar().getCarId())
                    .type(order.getType())
                    .inImgCode(order.getInImgCode())
                    .outImgCode(order.getOutImgCode())
                    .build();
            return RestResult.success(orderVo);
        }
    }
    
    /**
     * 
     * @param carId 车辆Id
     * @param car 车辆对象
     * @return
     */
    @PostMapping(value = "/needToPayByCar")
    @ApiOperation(value="根据车辆Id查询到需要付款的订单", notes="根据车辆Id查询到需要付款的订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<OrderVo> getNeedToPayByCar(
            @ApiParam(name="车牌号码", required=true) @RequestBody(required=true) CarParam carParam)
    {
        try
        {
            return RestResult.success(orderService.findNeedToPayByCar(carParam));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 使用微信支付订单
     */
    @PostMapping("/wxPay")
    @ApiOperation(value="支付订单", notes="发起订单支付")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<WxUnifiedOrderResult> payByWx(@ApiParam(name="订单支付参数", required=true) @RequestBody PayOrderParam payParam,
            Authentication auth)
    {
        try
        {
            return RestResult.success(orderService.payByWx(payParam, auth.getName()));
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }
    
    /**
     * 使用钱包支付订单
     */
    @PostMapping("/walletPay")
    @ApiOperation(value="支付订单", notes="发起订单支付")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Object> payByWallet(@ApiParam(name="订单支付参数", required=true) @RequestBody PayOrderParam payParam,
            Authentication auth)
    {
        try
        {
            orderService.payByWallet(payParam, auth.getName());
            return RestResult.success();
        }
        catch (Exception e)
        {
            return RestResult.failed(e.getMessage());
        }
    }

    /**
     * 微信付款通知
     */
    @PostMapping("/wxpay/notify")
    @ApiOperation(hidden = true, value = "")
    public String wxPayNotify() throws IOException, Exception
    {
        Map<String, String> retMap = new HashMap<String, String>();
        try
        {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
            String notifyData = sb.toString();
            Map<String, String> notifyMap = WXPayUtil.xmlToMap(notifyData);  // 转换成map
            
            //TODO: 打开校验
            /*if (!wxPay.isPayResultNotifySignatureValid(notifyMap)) 
            {
                // 签名错误，如果数据里没有sign字段，也认为是签名错误
                throw new Exception("签名错误");
            }*/
            
            // 签名正确,进行处理
            String return_code = (String) notifyMap.get("return_code");
            if (!return_code.equalsIgnoreCase(Const.WX_SUCCESS))
            {
                throw new BusinessException((String) notifyMap.get("return_msg"));
            }
            
            //交易标识
            String result_code = (String) notifyMap.get("result_code");
            if (!result_code.equalsIgnoreCase(Const.WX_SUCCESS))
            {
                throw new BusinessException((String)notifyMap.get("err_code_des"));
            }
            //读取支付结果参数
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            WxPayNotifyParam wxPayNotifyParam = WxPayNotifyParam.builder()
                    .openId(notifyMap.get("openid"))
                    .attach(notifyMap.get("attach"))
                    .bankType(notifyMap.get("bank_type"))
                    .isSubscribe(notifyMap.get("is_subscribe"))
                    .outTradeNo(notifyMap.get("out_trade_no"))
                    .transactionId(notifyMap.get("transaction_id"))
                    .timeEnd(simpleDateFormat.parse(notifyMap.get("time_end")))
                    .build();
            //处理订单状态,注意特殊情况：订单已经退款，但收到了支付结果成功的通知，不应把商户侧订单状态从退款改成支付成功
            orderService.wxPaySuccess(wxPayNotifyParam);
            retMap.put("return_code", "SUCCESS");
        }
        catch (Exception e)
        {
            retMap.put("return_code", "FAIL");
            retMap.put("return_msg", e.getMessage());
        }
        return WXPayUtil.mapToXml(retMap);
    }
    
    /**
     * 找到可以开票的订单
     */
    @GetMapping("/my/invoiceable")
    @ApiOperation(value="找到可以开票的订单", 
        notes="已经付款，未取消，未进入开票申请")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> myInvoiceable(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.invoiceable(auth.getName(), pageable));
    }
    

    /**
     * 找到等待付款的停车订单
     */
    @GetMapping("/my/parking/needToPay")
    @ApiOperation(value="找到等待付款的停车订单", 
        notes="1.订单拥有者是登录用户;2.订单拥有者为空，但是车辆绑定到登录用户的未支付订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> myNeedToPayParking(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.myNeedToPayParking(auth.getName(), pageable));
    }
    
    /**
     * 找到已经付款完成的停车订单
     */
    @GetMapping("/my/parking/payed")
    @ApiOperation(value="找到已经付款完成的停车订单", 
        notes="1.订单拥有者是登录用户;2.订单拥有者为空，但是车辆绑定到登录用户的未支付订单;3.已经付款和不需要付款的订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> myPayedParking(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.myPayedParking(auth.getName(), pageable));
    }
    
    /**
     * 我的钱包
     */
    @GetMapping("/my/wallet")
    @ApiOperation(value="我的钱包", notes="我的钱包变动记录")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> myWalletLogs(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.myWalletLogs(auth.getName(), pageable));
    }
    
    /**
     * 找到我已经付款的月票订单
     */
    @GetMapping("/my/monthlyTkt/payed")
    @ApiOperation(value="找到我已经付款的月票订单", notes="找到我已经付款的月票订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> myPayedMonthlyTkt(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.myPayedMonthlyTkt(auth.getName(), pageable));
    }
    
    /**
     * 找到等待付款的月票订单
     */
    @GetMapping("/my/monthlyTkt/needToPay")
    @ApiOperation(value="找到等待付款的月票订单", 
        notes="1.订单拥有者是登录用户;2.订单拥有者为空，但是车辆绑定到登录用户的未支付订单")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public RestResult<Page<OrderVo>> myNeedToPayMonthlyTkt(@ApiParam(name="分页信息",type="query") Pageable pageable,
            Authentication auth)
    {
        return RestResult.success(orderService.myNeedToPayMonthlyTkt(auth.getName(), pageable));
    }
    
    /**
     * 获取某次订单的入场记录截图
     * @throws IOException 
     */
    @GetMapping(value = "/image/in/{orderId}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ApiOperation(value="获取某次订单的入场记录截图", notes="获取某次订单的入场记录截图")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public byte[] getInImage(@ApiParam(name="订单Id",type="path") @PathVariable("orderId") Integer orderId,
            @ApiIgnore @PathVariable("orderId") Order order) throws IOException
    {
        return orderService.getOrderImage(order, DeviceUseage.in);
    }
    
    /**
     * 获取某次订单的出场记录截图
     * @throws IOException 
     */
    @GetMapping(value = "/image/out/{orderId}", produces = MediaType.IMAGE_JPEG_VALUE)
    @ApiOperation(value="获取某次订单的出场记录截图", notes="获取某次订单的出场记录截图")
    @ApiImplicitParam(name="Authorization", value="登录令牌", required=true, paramType="header")
    public byte[] getOutImage(@ApiParam(name="订单Id",type="path") @PathVariable("orderId") Integer orderId,
            @ApiIgnore @PathVariable("orderId") Order order) throws IOException
    {
        return orderService.getOrderImage(order, DeviceUseage.out);
    }
}
