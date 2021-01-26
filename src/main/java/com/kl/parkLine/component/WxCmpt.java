package com.kl.parkLine.component;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.feign.IWxFeignClient;
import com.kl.parkLine.json.WxSendMsgResult;
import com.kl.parkLine.json.WxTpltMsg;
import com.kl.parkLine.json.WxTpltMsgData;
import com.kl.parkLine.json.WxUnifiedOrderResult;
import com.kl.parkLine.service.AccessTokenService;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 */
@Component
public class WxCmpt
{
    private final String WX_MSG_TPLT_ID_MONTHLY_TKT_EXPIRE = "Z1Sq4OW1MA5IRhDq-sfPJwJ3wpRgV_RF9hwBPi8JmZ4";
    private final String COLOR_BLACK = "#000000";
    
    @Value("${wx.app.id}")
    private String appId;
    
    @Value("${wx.app.secret}")
    private String appSecret;
    
    private final String HQ = "HQ";
    private final String TRADE_TYPE = "JSAPI";
    
    @Value("${wxpay.notify.url}")  
    private String notifyUrl;
    
    @Autowired
    private WXPay wxPay;
    
    @Value("${spring.profiles.active}")
    private String active;
    
    @Value("${wx.pay.key}")
    private String wxPayKey;

    @Autowired
    private IWxFeignClient wxFeignClient;
    
    @Autowired
    private AccessTokenService accessTokenService;
    
    public WxUnifiedOrderResult unifiedOrder(Order order) throws Exception
    {
        Map<String, String> reqData = new HashMap<String, String>();
        
        //device_info
        if (null == order.getPark())
        {
            reqData.put("device_info", HQ);
        }
        else
        {
            reqData.put("device_info", order.getPark().getCode());
        }
        //body
        reqData.put("body", order.getType().getText());
        //out_trade_no
        reqData.put("out_trade_no", order.getCode());
        //total_fee, 将单位从元转换成分
        reqData.put("total_fee", order.getRealUnpayedAmt().multiply(new BigDecimal(100)).setScale(0).toString());
        //spbill_create_ip
        InetAddress address = InetAddress.getLocalHost();
        reqData.put("spbill_create_ip", address.getHostAddress());
        //notify_url
        reqData.put("notify_url", notifyUrl);
        //trade_type
        reqData.put("trade_type", TRADE_TYPE);
        //openid
        reqData.put("openid", order.getOwner().getWxOpenId());
        
        //调用接口
        Map<String, String> result = null;
        if (!active.equalsIgnoreCase("dev"))
        {
            result = wxPay.unifiedOrder(reqData);
        }
        else 
        {
            result = new HashMap<String, String>();
            result.put("return_code", Const.WX_SUCCESS);
            result.put("result_code", Const.WX_SUCCESS);
            result.put("prepay_id", "prepay_id01234");
        }
        
        //通信标识
        String return_code = (String) result.get("return_code");
        if (!return_code.equalsIgnoreCase(Const.WX_SUCCESS))
        {
            throw new BusinessException((String) result.get("return_msg"));
        }
        
        //交易标识
        String result_code = (String) result.get("result_code");
        if (!result_code.equalsIgnoreCase(Const.WX_SUCCESS))
        {
            throw new BusinessException((String) result.get("err_code_des"));
        }
        
        //二次签名
        String nonceStr = result.get("nonce_str");
        Map<String, String> signParams = new HashMap<String, String>();
        signParams.put("nonceStr", nonceStr);
        String pack = String.format("prepay_id=%s", result.get("prepay_id"));
        signParams.put("package", pack);
        signParams.put("signType", "MD5");
        signParams.put("appId", appId);
        Long timeStamp = System.currentTimeMillis() / 1000;
        signParams.put("timeStamp", timeStamp.toString());
        String paySign = WXPayUtil.generateSignature(signParams, wxPayKey);
        
        return WxUnifiedOrderResult.builder().nonceStr(nonceStr)
            .pack(pack).signType("MD5").appId(appId).timeStamp(timeStamp.toString())
            .paySign(paySign).orderCode(order.getCode()).build();
    }
    
    /**
     * 发送月票即将到期提醒
     * @param order
     * @return
     * @throws BusinessException 
     */
    public WxSendMsgResult sendMonthlyTktExpireNote(Order order) throws BusinessException
    {
        //获取有效令牌
        String accessToken = accessTokenService.getLatestToken();
        
        //构建消息
        Map<String, WxTpltMsgData> mapParams = new HashMap<String, WxTpltMsgData>();
        WxTpltMsg wxTpltMsg = WxTpltMsg.builder().toUser(order.getOwner().getWxOpenId())
                .templateId(WX_MSG_TPLT_ID_MONTHLY_TKT_EXPIRE)
                .data(mapParams).build();
        //first
        String content = String.format("尊敬的车主，您的爱车%s在%s的月卡即将到期，请记得及时续费哦！", 
                order.getCar().getCarNo(), order.getPark().getName());
        mapParams.put("first", WxTpltMsgData.builder().value(content).color(COLOR_BLACK).build());
        
        //keyword1,停车场
        mapParams.put("keyword1", WxTpltMsgData.builder().value(order.getPark().getName()).color(COLOR_BLACK).build());
        
        //keyword2,车牌号
        mapParams.put("keyword2", WxTpltMsgData.builder().value(order.getCar().getCarNo()).color(COLOR_BLACK).build());
        
        //keyword3,到期日
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        mapParams.put("keyword3", WxTpltMsgData.builder().value(simpleDateFormat.format(order.getEndDate())).color(COLOR_BLACK).build());
        
        //remark,备注
        mapParams.put("keyword3", WxTpltMsgData.builder().value("本停车场当前车位紧张，若不及时续费，可能会导致失去月租车位资格，没有车位停车哦，一定要记得及时续费哦！购买停车线月卡，省钱更省事，提前续费更便捷。").color(COLOR_BLACK).build());
        
        //发送消息
        return wxFeignClient.sendTpltMsg(accessToken, wxTpltMsg);
    }
}
