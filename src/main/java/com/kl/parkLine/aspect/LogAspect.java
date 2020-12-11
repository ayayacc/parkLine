package com.kl.parkLine.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
@Aspect
@Component
public class LogAspect
{
    private final static Logger logger = LoggerFactory.getLogger(LogAspect.class);
     
    @Pointcut("execution(* com.kl.parkLine.controller..*.*(..)) && !execution(* com.kl.parkLine.controller.BoyueController.comet(..)) ")
    public void log()
    {}
    
    @Around("log()")
    public Object aroundExec(ProceedingJoinPoint pdj)
    {
        StringBuilder sb = new StringBuilder();
        //方法名
        sb.append(String.format("Signature:%s;", pdj.getSignature().getName()));
        
        //参数
        Object[] os = pdj.getArgs();
        sb.append("Args:");
        for(int i = 0; i < os.length; i++)
        {
            String value = "null";
            if (null != os[i])
            {
                value = os[i].toString();
            }
            sb.append(String.format("arg[%d]:%s;", i, value));
        }
        
        //result为连接点的放回结果
        Object result = null;

        //执行目标方法
        try 
        {
            result = pdj.proceed();

            //返回通知方法
            if (null == result)
            {
                sb.append(String.format("Return: null"));
            }
            else
            {
                sb.append(String.format("Return: %s", result.toString()));
            }
            logger.info(sb.toString());
        } catch (Throwable e) {
            //异常
            e.printStackTrace();
            sb.append(String.format("Exception: %s", e));
            logger.error(sb.toString());
            throw new RuntimeException(e);
        } 

        return result;
    }
}
