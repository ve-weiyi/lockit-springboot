package com.ve.blog.exception;


import com.ve.blog.enums.StatusCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;


/**
 * 业务异常
 *
 * @author yezhiqiu
 * @date 2021/07/27
 */
@Getter
@AllArgsConstructor
public class BizException extends RuntimeException {

    /**
     * 错误码 200表示执行成功，其他都是失败
     */
    private Integer code = StatusCodeEnum.FAIL.getCode();

    /**
     * 错误信息
     */
    private String message ="系统错误";


    public BizException(String message) {
        this.message = message;
    }

    public BizException(StatusCodeEnum statusCodeEnum) {
        this.code = statusCodeEnum.getCode();
        this.message = statusCodeEnum.getDesc();
    }

    public BizException (String message, Throwable cause){
        this.message = message;
    }
}
