package com.ve.blog.handler;

import com.alibaba.fastjson.JSON;
import com.ve.blog.dao.RoleDao;
import com.ve.blog.dto.ResourceRoleDTO;
import com.ve.blog.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;

/**
 * 接口拦截规则
 * 根据请求url分析请求所需的角色
 * @author yezhiqiu
 * @date 2021/07/27
 */
@Component
public class FilterInvocationSecurityMetadataSourceImpl implements FilterInvocationSecurityMetadataSource {

    /**
     * 资源角色列表
     */
    private static List<ResourceRoleDTO> resourceRoleList;

    @Autowired
    private RoleDao roleDao;

    /**
     * 加载资源角色信息
     */
    @PostConstruct
    private void loadDataSource() {
        resourceRoleList = roleDao.listResourceRoles();
    }

    /**
     * 清空接口角色信息
     */
    public void clearDataSource() {
        resourceRoleList = null;
    }

    /**
     * 返回接口需要的角色列表
     * @param object
     * @return
     * @throws IllegalArgumentException
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        LogUtil.println("2.分析url需要的角色!");
        // 修改接口角色关系后重新加载
        if (CollectionUtils.isEmpty(resourceRoleList)) {
            this.loadDataSource();
        }
//        LogUtil.println(JSON.toJSONString(resourceRoleList));
        FilterInvocation fi = (FilterInvocation) object;
        // 获取用户请求方式
        String method = fi.getRequest().getMethod();
        // 获取用户请求Url
        String url = fi.getRequest().getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        // 获取接口角色信息，若为匿名接口则放行，若无对应角色则禁止
        for (ResourceRoleDTO resourceRoleDTO : resourceRoleList) {
            //请求地址、请求方式符合
            if (antPathMatcher.match(resourceRoleDTO.getUrl(), url) && resourceRoleDTO.getRequestMethod().equals(method)) {
                List<String> roleList = resourceRoleDTO.getRoleList();
                LogUtil.println("角色列表:"+JSON.toJSONString(roleList));
                if (CollectionUtils.isEmpty(roleList)) {
//                    return SecurityConfig.createList("disable");
                }else{
                    return SecurityConfig.createList(roleList.toArray(new String[]{}));
                }
            }
        }
        LogUtil.println("未匹配的 url:"+method+ url+" ，设置默认角色：游客");
        //没匹配的 url 默认登录就可以访问
        return null;
    }

    @Override
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        return null;
    }

    @Override
    public boolean supports(Class<?> aClass) {
        return FilterInvocation.class.isAssignableFrom(aClass);
    }

}
