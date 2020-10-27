package com.kl.parkLine.component;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.kl.parkLine.annotation.CompareValue;
import com.kl.parkLine.annotation.NeedToCompare;
import com.kl.parkLine.enums.OrderType;

@Component
public class Utils
{
    private final String EMPTY = "空";
    
    /**
     * 获取对象的null值字段
     * @param source
     * @return
     */
    public String[] getNullPropertyNames(Object source) 
    {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) 
        {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
    
    /**
     * 比较两个cert对象，得到不同字段的说明
     * @param objA
     * @param objB
     * @return
     */
    public String difference(Object objA, Object objB)
    {
        StringBuilder difference = new StringBuilder();
        
        //遍历所有字段，查看是否有需要比对的类型
        Field[] fields = objA.getClass().getDeclaredFields();
        for (Field field : fields)
        {
            //未声明@NeedToCompare
            if (false == field.isAnnotationPresent(NeedToCompare.class))
            {
                continue;
            }
            
            //取到字段值比对
            final BeanWrapper bwA = new BeanWrapperImpl(objA);
            final BeanWrapper bwB = new BeanWrapperImpl(objB);
            Object valueA = bwA.getPropertyValue(field.getName());
            Object valueB = bwB.getPropertyValue(field.getName());
            
            //比对值是否相等
            NeedToCompare antNeedToCompare = field.getAnnotation(NeedToCompare.class); 
            if (valueA instanceof Timestamp)
            {
                valueA = ((Timestamp)valueA).getTime();
                if (null != valueB)
                {
                    valueB = ((Date)valueB).getTime();
                }
            }
            valueA = formatEmpty(valueA);
            valueB = formatEmpty(valueB);
            //判断两个值是否相等
            if (false == valueA.equals(valueB))
            {
                //记录区别信息
                valueA = formatValue(valueA, field);
                valueB = formatValue(valueB, field);
                difference.append(String.format("<li><b>%s</b>: %s--->%s</li>", antNeedToCompare.name(),
                        valueA, valueB));
            }
        }
        if (0 == difference.length())
        {
            return null;
        }
        else
        {
            return String.format("<ol>%s</ol>", difference.toString());
        }
    }
    
    /**
     * 预处理空值
     * @param value
     * @return
     */
    private Object formatEmpty(Object value)
    {
        if (null == value)
        {
            return EMPTY;
        }
        if (value instanceof String)
        {
            if (StringUtils.isEmpty(value.toString()))
            {
                return EMPTY;
            }
        }
        return value;
    }
            
    /**
     * 格式化空字符串
     * @param value
     * @return
     */
    public String formatValue(Object value, Field field)
    {
        //预先处理null
        value = formatEmpty(value);
        if (value.toString().equalsIgnoreCase(EMPTY))
        {
            return EMPTY;
        }

        //如果需要取类属性的指定字段
        if (field.getType().isAnnotationPresent(CompareValue.class))
        {
            CompareValue antCompareValue = (CompareValue)field.getType().getAnnotation(CompareValue.class); 
            final BeanWrapper bwValue = new BeanWrapperImpl(value);
            value = (String) bwValue.getPropertyValue(antCompareValue.field());
        }
        //日期格式化
        else if (field.isAnnotationPresent(DateTimeFormat.class))
        {
            Date date = null;
            if (value instanceof Long)
            {
                date = new Date((Long)value);
            }
            else
            {
                date = (Date)value;
            }
            DateTimeFormat antDateTimeFormat = (DateTimeFormat)field.getAnnotation(DateTimeFormat.class); 
            SimpleDateFormat sdf = new SimpleDateFormat(antDateTimeFormat.pattern());
            value = sdf.format(date);
        }
        
        return value.toString();
    }
    
    /**
     * 在一般情况下使用Request.getRemoteAddr()即可，但是经过nginx等反向代理软件后，这个方法会失效。
     * 本方法先从Header中获取X-Real-IP，如果不存在再从X-Forwarded-For获得第一个IP(用,分割)，
     * 如果还不存在则调用Request .getRemoteAddr()。
     * @param request
     * @return
     */
    public static String getIp(HttpServletRequest request) 
    {
        String ip = request.getHeader("X-Real-IP");
        if (null != ip 
            && false == ip.isEmpty() 
            && false == ip.equalsIgnoreCase("unknown"))
        {
            return ip;
        }
        ip = request.getHeader("X-Forwarded-For");
        if (null != ip 
            && false == ip.isEmpty() 
            && false == ip.equalsIgnoreCase("unknown")) 
        {
            int index = ip.indexOf(",");
            if (-1 != index) 
            {
                return ip.substring(0, index);
            } 
            else 
            {
                return ip;
            }
        } 
        else 
        {
            return request.getRemoteAddr();
        }
    }
    
    /**
     * 构建订单编码
     * @param type
     * @return 订单编码
     */
    public String makeCode(OrderType type)
    {
        Date now = new Date();
        String prefix = "";
        switch (type)
        {
            case parking:  //停车
                prefix = "TC";
                break;
            case monthlyTicket: //月票
                prefix = "YP";
                break;
            case coupon:  //优惠券
                prefix = "YHQ";
                break;
            case walletIn: //钱包充值
                prefix = "CZ";
                break;
            default:
                break;
        }
        String code = prefix + String.valueOf(now.getTime());
        return code;
    }
}
