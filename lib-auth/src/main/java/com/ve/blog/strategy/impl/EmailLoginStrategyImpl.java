package com.ve.blog.strategy.impl;

import com.alibaba.fastjson.JSON;
import com.ve.blog.application.QQConfigProperties;
import com.ve.blog.dto.*;
import com.ve.blog.entity.UserAuth;
import com.ve.blog.enums.LoginTypeEnum;
import com.ve.blog.exception.BizException;
import com.ve.blog.util.BeanCopyUtils;
import com.ve.blog.util.IpUtils;
import com.ve.blog.vo.LoginVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

import static com.ve.blog.constant.CommonConst.TRUE;

/**
 * 本地登录策略实现
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Service("emailLoginStrategyImpl")
public class EmailLoginStrategyImpl extends AbstractSocialLoginStrategyImpl {


    @Override
    public UserInfoDTO login(String data) {
        // 返回用户信息
        return null;
    }

    @Override
    public SocialTokenDTO getSocialToken(String data) {
        LoginVO loginVO = JSON.parseObject(data, LoginVO.class);

        // 返回token信息
        return null;
    }

    @Override
    public SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO) {
        return null;
    }

}
