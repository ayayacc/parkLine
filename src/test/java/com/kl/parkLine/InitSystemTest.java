package com.kl.parkLine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.kl.parkLine.dao.IDictDao;
import com.kl.parkLine.dao.IParkDao;
import com.kl.parkLine.entity.Dict;
import com.kl.parkLine.entity.Park;

@SpringBootTest
@EnableJpaAuditing(auditorAwareRef = "mockAuditorAware")
public class InitSystemTest
{
    @Autowired 
    protected MockAuditorAwareTest mockAuditorAware; 
    
    @Autowired
    private IDictDao dictDao;
    
    @Autowired
    private IParkDao parkDao;
    
    @BeforeEach
    public void wireUpAuditor()
    {
        mockAuditorAware.setCurrentAuditor("admin");
    }
    
    //初始化字典
    @Test
    @Transactional
    @Rollback(false)
    public void initSystem() throws Exception
    {
        initDict();
        initPark();
    }
    
    /**
     * 初始化字典
     * @throws Exception
     */
    private void initDict() throws Exception
    {
        String[][] dictsStr = 
        {
            //性别
            {"GENDER", "性别", "", "0100"},
            //性别,男
            {"GENDER_MALE", "男", "GENDER", "0101"},
            //性别,女
            {"GENDER_FEMALE", "女", "GENDER", "0102"},
            //订单状态
            {"ORDER_STATUS", "订单状态", "", "0200"},
            //已入场
            {"ORDER_STATUS_IN", "已入场", "ORDER_STATUS", "0201"},
            //待支付（已出场）
            {"ORDER_STATUS_NEED_TO_PAY", "待支付", "ORDER_STATUS", "0202"},
            //已支付
            {"ORDER_STATUS_PAYED", "已支付", "ORDER_STATUS", "0203"},
            //开票中
            {"ORDER_STATUS_INVOICING", "开票中", "ORDER_STATUS", "0204"},
            //已开票
            {"ORDER_STATUS_INVOICED", "已开票", "ORDER_STATUS", "0205"},
            //已取消
            {"ORDER_STATUS_CANCELED", "已取消", "ORDER_STATUS", "0206"},
            //支付方式:微信/钱包
            {"PAYMENT_TYPE", "支付方式", "", "0300"},
            //微信
            {"PAYMENT_TYPE_WX", "微信", "PAYMENT_TYPE", "0301"},
            //钱包
            {"PAYMENT_TYPE_QB", "钱包", "PAYMENT_TYPE", "0302"},
            //订单类型:停车订单/月票订单/优惠券激活订单/钱包充值订单
            {"ORDER_TYPE", "订单类型", "", "0400"},
            //停车订单
            {"ORDER_TYPE_PARK", "停车", "ORDER_TYPE", "0401"},
            //月票订单
            {"ORDER_TYPE_MONTHLY_TICKET", "月票", "ORDER_TYPE", "0402"},
            //优惠券激活订单
            {"ORDER_TYPE_COUPON", "优惠券激活", "ORDER_TYPE", "0403"},
            //钱包充值订单
            {"ORDER_TYPE_WALLET_IN", "钱包充值", "ORDER_TYPE", "0404"},
            //钱包提现订单
            {"ORDER_TYPE_WALLET_OUT", "钱包提现", "ORDER_TYPE", "0405"},
            //发票状态：已申请/开票成功/开票失败
            {"INVOICE_STATUS", "发票状态", "", "0500"},
            //已申请
            {"INVOICE_STATUS_SUBMITED", "已申请", "INVOICE_STATUS", "0501"},
            //开票成功
            {"INVOICE_STATUS_SUCCESSED", "开票成功", "INVOICE_STATUS", "0502"},
            //开票失败
            {"INVOICE_STATUS_FAILED", "开票失败", "INVOICE_STATUS", "0503"},
            //优惠券状态: 未开始/有效/已使用/无效/已过期
            {"COUPON_STATUS", "优惠券状态", "", "0600"},
            //未开始
            {"COUPON_STATUS_NOT_START", "未开始", "COUPON_STATUS", "0601"},
            //有效
            {"COUPON_STATUS_VALID", "有效", "COUPON_STATUS", "0602"},
            //已使用
            {"COUPON_STATUS_USED", "已使用", "COUPON_STATUS", "0603"},
            //无效
            {"COUPON_STATUS_INVALID", "无效", "COUPON_STATUS", "0605"},
            //已过期
            {"COUPON_STATUS_EXPIRED", "已过期", "COUPON_STATUS", "0606"},
            //车牌颜色：未知/蓝/黄/黑/白/绿
            {"PLATE_COLOR", "车牌颜色", "", "0700"},
            //未知
            {"PLATE_COLOR_UNKNOWN", "未知", "PLATE_COLOR", "0701"},
            //蓝
            {"PLATE_COLOR_BLUE", "蓝", "PLATE_COLOR", "0702"},
            //黄
            {"PLATE_COLOR_YELLOW", "黄", "PLATE_COLOR", "0703"},
            //黑
            {"PLATE_COLOR_BLACK", "黑", "PLATE_COLOR", "0705"},
            //白
            {"PLATE_COLOR_WHITE", "白", "PLATE_COLOR", "0707"},
            //绿
            {"PLATE_COLOR_GREEN", "绿", "PLATE_COLOR", "0708"},
            //异常停车类型
            {"PARK_ABNORMAL", "异常停车类型", "", "0800"},
            //正常
            {"PARK_ABNORMAL_ZC", "正常", "PARK_ABNORMAL", "0801"},
            //跨位
            {"PARK_ABNORMAL_KW", "跨位", "PARK_ABNORMAL", "0802"},
            //斜位
            {"PARK_ABNORMAL_XW", "斜位", "PARK_ABNORMAL", "0803"},
            //压线
            {"PARK_ABNORMAL_YX", "压线", "PARK_ABNORMAL", "0804"},
            //事件类型：入/出/完成
            {"EVENT_TYPE", "异常停车类型", "", "0900"},
            //入
            {"EVENT_TYPE_CAR_IN", "入", "EVENT_TYPE", "0901"},
            //出
            {"EVENT_TYPE_CAR_OUT", "出", "EVENT_TYPE", "0902"},
            //完成
            {"EVENT_TYPE_CAR_COMPLETE", "完成", "EVENT_TYPE", "0903"},
            //完成
            {"EVENT_TYPE_CANCLE", "取消", "EVENT_TYPE", "0904"}
        };
        
        //保存字典
        for (String[] dictStr : dictsStr)
        {
            String code = dictStr[0];
            Dict dict = dictDao.findOneByCodeAndEnabled(code, "Y");
            if (null == dict)
            {
                dict = new Dict();
            }
            dict.setEnabled("Y");
            dict.setCode(code);
            dict.setText(dictStr[1]);
            if (!StringUtils.isEmpty(dictStr[2]))
            {
                Dict parent = dictDao.findOneByCodeAndEnabled(dictStr[2], "Y");
                if (null == parent)
                {
                    throw new Exception(String.format("Invalid parent code: %s for %s", 
                            dictStr[2], dictStr[0]));
                }
                dict.setParentDict(parent);
            }
            dict.setSortIdx(dictStr[3]);
            dictDao.save(dict);
        }
    }
    
    /**
     * 初始化停车场
     */
    private void initPark()
    {
        String[][] parksStr = {
            {"parkCode01", "parkName01", "100", "108.2815 22.9033", "parkContact01"}
        };
        
        for (String[] parkStr : parksStr)
        {
            String code = parkStr[0];
            Park park = parkDao.findOneByCode(code);
            if (null == park)
            {
                park = new Park();
            }
            park.setEnabled("Y");
            park.setCode(code);
            park.setName(parkStr[1]);
            park.setTotalCnt(Integer.valueOf(parkStr[2]));
            park.setGeo(parkStr[3]);
            park.setContact(parkStr[4]);
            parkDao.save(park);
        }
    }
}
