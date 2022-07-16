package com.ve.blog.application;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description create for blog-springboot-plus .
 * @Author weiyi
 * @Date 2022/7/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @Value("${jwt.secret}")//jwt 密钥
    private String secret;

    @Value("${jwt.expiration}")//失效时间
    private Long expiration;
}
