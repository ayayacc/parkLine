package com.kl.parkLine.component;

import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.github.wxpay.sdk.WXPay;
import com.github.wxpay.sdk.WXPayUtil;
import com.kl.parkLine.entity.Order;
import com.kl.parkLine.exception.BusinessException;
import com.kl.parkLine.json.WxCode2SessionResult;
import com.kl.parkLine.json.WxunifiedOrderResult;
import com.kl.parkLine.util.Const;

/**
 * @author chenc
 */
@Component
public class WxCmpt
{
    //TODO: 设置小程序参数
    private final String APP_SECRET = "5ff32eb6bfd676e8ca93289e333b54cf";
    private final String APP_ID = "5ff32eb6bfd676e8ca93289e333b54cf";
    private final String API_KEY = "5ff32eb6bfd676e8ca93289e333b54cf";
    
    private final String HQ = "HQ";
    private final String TRADE_TYPE = "JSAPI";
    
    @Value("${wxpay.notify.url}")  
    private String notifyUrl;
    
    @Autowired
    private WXPay wxPay;
    
    @Autowired
    private WXMappingJackson2HttpMessageConverter jsonConverter;
    
    public WxCode2SessionResult code2Session(String jsCode) throws Exception
    {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid={APPID}&secret={APPSECRET}&js_code={JSCODE}&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(jsonConverter);
        WxCode2SessionResult result = restTemplate.getForObject(url, WxCode2SessionResult.class, APP_ID, APP_SECRET, jsCode);
        if (false == StringUtils.isEmpty(result.getErrmsg()))
        {
            throw new Exception(String.format("Get WeChat jscode2session failed:%d, %s", 
                    result.getErrcode(), result.getErrmsg()));
        }
        return result;
    }
    
    public WxunifiedOrderResult unifiedOrder(Order order) throws Exception
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
        reqData.put("total_fee", order.getAmt().multiply(new BigDecimal(100)).toString());
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
        Map<String, String> result = wxPay.unifiedOrder(reqData);
        
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
        Map<String, String> signParams = new HashMap<String, String>();
        String nonceStr = WXPayUtil.generateNonceStr();
        signParams.put("nonceStr", nonceStr);
        String pack = String.format("prepay_id=%s", result.get("prepay_id"));
        signParams.put("package", pack);
        signParams.put("signType", "MD5");
        signParams.put("appId", APP_ID);
        Long timeStamp = System.currentTimeMillis() / 1000;
        signParams.put("timeStamp", timeStamp.toString());
        String paySign = WXPayUtil.generateSignature(signParams, API_KEY);
        signParams.put("paySign", paySign);
        
        return WxunifiedOrderResult.builder().nonceStr(nonceStr)
            .pack(pack).signType("MD5").appId(APP_ID).timeStamp(timeStamp.toString())
            .paySign(paySign).build();
    }
    
}
