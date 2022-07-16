package com.ve.blog.config;

import com.ve.blog.handler.*;
import com.ve.blog.handler.*;
import com.ve.blog.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.HttpSessionEventPublisher;


/**
 * Security配置类
 * @author yezhiqiu
 * @date 2021/07/29
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;
    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;
    @Autowired
    private AuthenticationSuccessHandlerImpl authenticationSuccessHandler;
    @Autowired
    private AuthenticationFailHandlerImpl authenticationFailHandler;
    @Autowired
    private LogoutSuccessHandlerImpl logoutSuccessHandler;

    @Bean
    public FilterInvocationSecurityMetadataSource securityMetadataSource() {
        return new FilterInvocationSecurityMetadataSourceImpl();
    }

    @Bean
    public AccessDecisionManager accessDecisionManager() {
        return new AccessDecisionManagerImpl();
    }

    @Bean
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }


    /**
     * 密码加密
     * @return {@link PasswordEncoder} 加密方式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /**
     * 身份认证接口,new 出来的无法 Autowire
     */
//    @Override
//    protected void configure(AuthenticationManagerBuilder auth) throws Exception
//    {
//        auth.userDetailsService(new UserDetailsServiceImpl()).passwordEncoder(passwordEncoder());
//    }

    /**
     * 配置权限
     * @param http http
     * @throws Exception 异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 关闭跨站请求防护，设置错误拦截器
        http.csrf().disable();

        //基于token，所以不需要session
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                .and()
//                //添加jwt 登录授权过滤器
//                .addFilterBefore(new JwtAuthenticationTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        //使用session，登录用户数据保存在 Session 中
        http.sessionManagement()
                .maximumSessions(20)
                .sessionRegistry(sessionRegistry());

        /**
         * 表单登录相关的配置
         */
        http.formLogin()
                // 前端登录表单用户名别名, 从参数user中获取username参数取值
                .usernameParameter("username")
                // 前端登录表单密码别名, 从参数passwd中获取password参数取值
                .passwordParameter("password")
                //登录页面，get http://localhost:8080/login
                .loginPage("/login")
                //登录请求, post  http://localhost:8080/login
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailHandler)
                .permitAll();//不拦截

        http.logout()
                .logoutUrl("/logout")
                .logoutSuccessHandler(logoutSuccessHandler)
//                .deleteCookies()//清除cookie
//                .clearAuthentication(true)//清除权限相关
//                .invalidateHttpSession(true)//清除session
                .permitAll();

        http.exceptionHandling()
                // 403 权限不足处理
                .accessDeniedHandler(accessDeniedHandler)
                // 未登录处理
                .authenticationEntryPoint(authenticationEntryPoint);


        /**
         * http请求是否要登录认证配置
         */
        http.authorizeRequests()
                // 允许GET请求登录页面匿名访问
                .antMatchers(HttpMethod.GET, "/login", "/logout", "/captcha").anonymous()
                // 用户具有admin角色时允许访问/role
                .antMatchers(HttpMethod.GET, "/role").hasRole("admin")
                // 用户具有system:user权限时允许访问/role
                .antMatchers(HttpMethod.GET, "/role").hasAuthority("system:user")
                //除上面外，所有请求都要求认证
                .anyRequest().authenticated()
                //动态权限配置
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setAccessDecisionManager(accessDecisionManager());
                        object.setSecurityMetadataSource(securityMetadataSource());
                        return object;
                    }
                })
                .and()
                //记住密码
                .rememberMe()
                .and()
                //禁用缓存
                .headers()
                .cacheControl();


    }


    /**
     * WebSecurity不走过滤链 通俗的说 不经过各种复杂的filter 直接就放行了
     * login、logout 不要在此处设置，否则会跳转error
     * 放行路径，对于不需要授权的静态文件放行
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        //放行静态资源
        web.ignoring().antMatchers(
                "/ws/**",
                "/static/**",
                "/css/**",
                "/js/**",
                "/index.html",
                "/favicon.ico",
                "/doc.html",
                "/webjars/**",
                "/swagger-resources/**",
                "/v2/api-docs/**",
                "/captcha",
                "/hello",
                "/public/**");
    }
}
