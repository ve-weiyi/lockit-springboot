package com.ve.blog.handler;

import com.alibaba.fastjson.JSON;
import com.ve.blog.util.LogUtil;
import com.ve.blog.vo.Result;
import com.ve.blog.constant.CommonConst;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 注销处理
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Component
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication) throws IOException {
        httpServletResponse.setContentType(CommonConst.APPLICATION_JSON);
        httpServletResponse.getWriter().write(JSON.toJSONString(Result.ok()));
        LogUtil.println("登录成功！");
    }

}
