package com.ve.blog.handler;

import com.ve.blog.enums.StatusCodeEnum;
import com.ve.blog.exception.BizException;
import com.ve.blog.util.LogUtil;
import com.ve.blog.vo.Result;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Objects;

/**
 * @Description create for blog-springboot .
 * 全局异常处理
 * @Author weiyi
 * @Date 2022/5/9
 * @RestControllerAdvice
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义的业务异常
     */
    @ExceptionHandler(value = BizException.class)
    public Result<?> bizExceptionHandler(HttpServletRequest req, BizException exception) {
        return Result.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> httpExceptionHandler(MethodArgumentNotValidException e) {
        return Result.fail(StatusCodeEnum.VALID_ERROR.getCode(), Objects.requireNonNull(e.getBindingResult().getFieldError()).getDefaultMessage());
    }

    /**
     * 其他异常
     */
    @ExceptionHandler(value=Exception.class)
    public Result<?>  exceptionHandler(HttpServletRequest req, Exception e){
        //  get post put
        String method = req.getMethod();
        //  blog/api
        String conPath = req.getContextPath();
        //  localhost
        String pathInfo = req.getServerName();
        LogUtil.println(method);
        LogUtil.println(conPath);
        LogUtil.println(pathInfo);

        if(e instanceof NullPointerException){
            return Result.fail("空指针异常！"+e.getMessage());
        }

        if(e instanceof SQLException){
            if (e instanceof SQLIntegrityConstraintViolationException){
                return Result.fail("该数据有关联数据，操作失败！"+e.getMessage());
            }
            return Result.fail("数据库异常！"+e.getMessage());
        }

        if(e instanceof ExpiredJwtException){
            return Result.fail("用户信息已过期,请重新登录！"+e.getMessage());
        }
        e.printStackTrace();
        return Result.fail(e.getLocalizedMessage());
    }

}
