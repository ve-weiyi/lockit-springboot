package com.ve.blog.handler;

import com.alibaba.fastjson.JSON;
import com.ve.blog.util.LogUtil;
import com.ve.blog.vo.Result;
import com.ve.blog.constant.CommonConst;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录失败处理
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Component
public class AuthenticationFailHandlerImpl implements AuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException {
        LogUtil.println("登录失败！");
        httpServletResponse.setContentType("application/json;charset=UTF-8");
        httpServletResponse.getWriter().write(JSON.toJSONString(Result.fail(e.getMessage())));
    }

}
