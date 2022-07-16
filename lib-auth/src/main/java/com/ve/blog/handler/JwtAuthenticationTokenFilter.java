package com.ve.blog.handler;


import com.ve.blog.dto.UserDetailDTO;
import com.ve.blog.service.impl.UserDetailsServiceImpl;
import com.ve.blog.util.JwtTokenUtil;
import com.ve.blog.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Jwt登录授权过滤器，在登录时验证token是否有效
 * @author zhoubin
 * @since 1.0.0
 */
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    /**
     * AbstractAuthenticationToken实现了两个主要的接口，
     * 用于保存身份主体信息的Principal和Authentication一支，用于保存认证凭证比如密码的CredentialsContainer。
     *
     * @param httpServletRequest
     * @param httpServletResponse
     * @param chain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain) throws ServletException,
    IOException {
        //通过 request 获取请求头
        String authHeader = httpServletRequest.getHeader(tokenHeader);
        LogUtil.println("1.jwt拦截!");
        //验证头部，不存在，或者不是以tokenHead：Bearer开头的
        if (authHeader != null && authHeader.startsWith(tokenHead)){
            LogUtil.println("jwt令牌："+authHeader);

            //存在，就做一个字符串的截取，其实就是获取了登录的token
            String authToken = authHeader.substring(tokenHead.length());
            //jwt根据token获取用户名
            //token存在用户名但是未登录
            String userName = jwtTokenUtil.getUserNameFromToken(authToken);

            LogUtil.println("存在token，并且是以head开头的-->"+userName);
            if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null){
                LogUtil.println("token有效，授权成功-->登录用户："+userName);
                //登录
                UserDetailDTO userDetails = userDetailsService.loadUserByUsername(userName);
                //判断token是否有效，如果有效把他重新放到用户对象里面
                if (jwtTokenUtil.validateToken(authToken,userDetails)){
                    LogUtil.println("token有效且未过期-->设置全局对象。");
                    //设置SecurityContext(principal,credentials,< extends GrantedAuthority> authorities)
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,null,userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
        }else{
            LogUtil.println("无令牌或令牌格式不正确-->"+authHeader);
        }
        //放行，但没有setAuthentication，后续会拦截
        // doFilter执行后即是验证成功
        chain.doFilter(httpServletRequest,httpServletResponse);
    }
}