package com.ve.blog.application;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Description create for blog-springboot-plus .
 * 作者信息
 * @Author weiyi
 * @Date 2022/7/8
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "author")
public class AuthorProperties {

    public  String name;

    public  String github;

    public  String qq;

    public  String email;
}
