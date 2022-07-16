package com.ve.blog.handler;

import com.ve.blog.util.LogUtil;
import com.ve.blog.util.LogUtil;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 访问决策管理器
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Component
public class AccessDecisionManagerImpl implements AccessDecisionManager {
    @Override
    public void decide(Authentication authentication, Object o, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        LogUtil.println("3.权限认证！");

        // 用户拥有的权限列表
        List<String> permissionList = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());


        // 接口需要的角色列表
        for (ConfigAttribute configAttribute : configAttributes) {
            //当前 url 所需要的角色，此角色在CustomFilter中设置
            String needRole = configAttribute.getAttribute();

            if (needRole.isEmpty()) {
                LogUtil.println("无需权限，可以访问！");
                return;
            }

            if (permissionList.contains(needRole)) {
                LogUtil.println("角色符合，通过认证！");
                return;
            }

            //禁止访问
            if ("disable".equals(needRole)){
                if (authentication instanceof AnonymousAuthenticationToken){
                    LogUtil.println("令牌错误！");
                    throw new AccessDeniedException("尚未登录，请登录！");
                }else {
                    LogUtil.println("未指定资源所需角色，默认通过认证！");
                    return;
                }
            }
        }
        LogUtil.println("无权限！->"+configAttributes);
        throw new AccessDeniedException("没有操作权限！");
    }

    @Override
    public boolean supports(ConfigAttribute configAttribute) {
        return true;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return true;
    }
}
