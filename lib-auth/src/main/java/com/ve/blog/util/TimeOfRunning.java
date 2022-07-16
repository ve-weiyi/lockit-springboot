package com.ve.blog.util;

import java.text.SimpleDateFormat;

/**
 * @Description hello word!
 * @Author weiyi
 * @Date 2022/1/5
 */
public class TimeOfRunning {
    private long startMilli;//开始时间
    private long endMilli;//结束时间
    private String tip;

    public TimeOfRunning(String tip){
        this.tip=tip;
    }

    public TimeOfRunning start(){
        startMilli = System.currentTimeMillis();// 当前时间对应的毫秒数

        LogUtil.println("/**开始 " + getTimeMillis(startMilli));
        return this;
    }

    public TimeOfRunning end(){
        endMilli = System.currentTimeMillis();//结束时间
        LogUtil.println("/**结束 s" + getTimeMillis(endMilli));
        LogUtil.println("/**" + tip + "总耗时为：" + (endMilli - startMilli) + "毫秒");
        return this;
    }

    private String getTimeMillis(Long currentTimeMillis) {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
        return format.format(currentTimeMillis);
    }
}
