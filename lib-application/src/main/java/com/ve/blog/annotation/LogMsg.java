package com.ve.blog.annotation;

import java.lang.annotation.*;

/**
 * @Description create for auth .
 * 注解，基于spring容器实现
 * 作者：prestlhh https://www.bilibili.com/read/cv13282317 出处：bilibili
 * 元注解：
 *  @Target：表明该注解可以应用的java元素类型
 *      ElementType.METHOD 用于方法
 *      ElementType.FIELD 用于属性
 *  @Retention：表明该注解的生命周期
 *      RetentionPolicy.RUNTIME 在运行时可获取
 *  @Document：描述注解是否被抽取到api文档中
 *  @Inherited：描述注解是否被子类继承
 *
 *
 * @Author weiyi
 * @Date 2022/7/6
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogMsg {

    String tag() default "标签";

    String msg() default "消息";

    int time() default 2022;
}