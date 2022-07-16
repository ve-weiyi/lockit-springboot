package com.ve.blog.annotation;

import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * @Description create for auth .
 *
 * @Author weiyi
 * @Date 2022/7/6
 */
@Aspect
@Component
public class LogMsgAspect {


    /**
     * 目标类
     * 定义一个可以在@AspectJ切面内可重用的切点，切点的名称来源于注解所应用的方法名称，方法本身只是一个标识，供@pointcut注解依附
     */
    @Pointcut("@annotation(com.ve.blog.annotation.LogMsg)")
    public void log(){

    }

    @Before("log()")
    public void start(){
        System.out.println("start invoke method");
    }

    @After("log()")
    public void end(){
        System.out.println("end invoke method");
    }

    /**
     * 标识调用结果返回后，该方法被调用
     */
    @AfterReturning("log()")
    public void ret(){
        System.out.println("invoke method return");
    }

    /**
     * 标识调用抛出异常时，该方法被调用
     */
    @AfterThrowing("log()")
    public void thr(){
        System.out.println("invoke method error");
    }
}
