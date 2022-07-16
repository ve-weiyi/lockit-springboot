package com.ve.blog.util;

import org.slf4j.LoggerFactory;

/**
 * @Description hello word!
 *  logback使用日志工具类静态方法打印日志
 *  https://blog.csdn.net/z562743237/article/details/78780070
 *
 *  日志工具类，使用静态方法打印日志  无需每个类中定义日志对象
 *  Logback对每个Logger对象做了缓存，每次调用LoggerFactory.getLogger(String name)时如果已存在则从缓存中获取不会生成新的对象;
 *  同时也不会有对象的创建与销毁造成的性能损失
 * @Author weiyi
 * @Date 2022/1/6
 */
public class LogUtil {

   // private final static LogMsg log = LoggerFactory.getLogger(LogUtil.class);
    public static final int VERBOSE = 1;
    public static final int DEBUG = 2;
    public static final int INFO = 3;
    public static final int WARN = 4;
    public static final int ERROR = 5;
    public static final int NOTHING = 6;
    
    public static int level = 2;
    

    private static int MAX_INFO_LENGTH=1000;

    public static void println(String msg) {
        LoggerFactory.getLogger(getClassName()).info(generateMessage(msg));
      //  LoggerFactory.getLogger(getClassName()).debug(msg);
    }

    public static void println(Object... objects) {
        System.out.println("-->> "+objects);
    }

    public static void println() {
        System.out.println();
    }

     // 获取打印信息所在方法名，行号等信息
    private static String[] autoJumpLogInfos()
    {
        String[] infos = new String[3];
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        int stackNumber= 4;
        infos[0] = elements[stackNumber].getClassName().substring(elements[stackNumber].getClassName().lastIndexOf(".") + 1);
        infos[1] = elements[stackNumber].getMethodName();
        infos[2] = "(" + elements[stackNumber].getFileName() + ":" + elements[stackNumber].getLineNumber() + ")";
        return infos;
    }

    // 生成Test(RSAUtils.java:466) -->> 前缀，点击可以跳转
    public static String generateMessage(String msg) {
        String[] info=autoJumpLogInfos();
        int msgLength=msg.length();
        int start=0;
        int end=Math.min(MAX_INFO_LENGTH,msgLength);
        String result=info[1]+info[2]+ " -->> "+ msg.substring(start,end);
        return result;
    }

    public static void error(String msg) {
        LoggerFactory.getLogger(getClassName()).error(msg);
    }

    public static void error(String msg, Object... obj) {
        LoggerFactory.getLogger(getClassName()).error(msg, obj);
    }

    public static void warn(String msg) {
        LoggerFactory.getLogger(getClassName()).error(msg);
    }

    public static void warn(String msg, Object... obj) {
        LoggerFactory.getLogger(getClassName()).error(msg, obj);
    }

    public static void info(String msg) {
        LoggerFactory.getLogger(getClassName()).info(msg);
    }

    public static void info(String msg, Object... obj) {
        LoggerFactory.getLogger(getClassName()).info(msg, obj);
    }

    public static void debug(String msg) {
        LoggerFactory.getLogger(getClassName()).debug(msg);
    }

    public static void debug(String msg, Object... obj) {
        LoggerFactory.getLogger(getClassName()).debug(msg, obj);
    }

    // 获取调用 error,info,debug静态类的类名
    private static String getClassName() {
        return new SecurityManager() {
            public String getClassName() {
                return getClassContext()[3].getName();
            }
        }.getClassName();
    }
}
