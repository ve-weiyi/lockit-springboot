package com.ve.blog.config;


import com.github.xiaoymin.knife4j.spring.extension.OpenApiExtensionResolver;
import com.ve.blog.application.AuthorProperties;
import com.ve.blog.application.ProjectProperties;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Knife4j配置类
 *
 * @author yezhiqiu
 */
@Configuration
@EnableSwagger2WebMvc
public class Knife4jConfig {

    @Value("${server.port}")
    public static String port;


    /**
     *  增强功能必须yml 和 该扩展配合使用，否则无法生效
     */
    private final OpenApiExtensionResolver openApiExtensionResolver;

    @Autowired
    public Knife4jConfig(OpenApiExtensionResolver openApiExtensionResolver) {
        this.openApiExtensionResolver = openApiExtensionResolver;
    }

    @Autowired
    private AuthorProperties author;

    @Autowired
    private ProjectProperties project;


    @Bean
    public Docket createRestApi() {
        String GROUP_NAME=project.name+"-"+project.version;
        // 添加自定义文档要对应 knife4j 版本选择 doc 类型， 否则该功能不生效
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                //设置ip和端口，或者域名
                .host(project.host)
                // 用来创建该API的基本信息，展示在文档的页面中（自定义展示的信息）
                .apiInfo(apiInfo())
                .groupName(GROUP_NAME)
                //启动用于api选择的生成器
                .select()
                //为有@Api注解的Controller生成API文档
                .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                //为有@ApiOperation注解的方法生成API文档
//                .apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
                //为任何接口生成API文档
//                .apis(RequestHandlerSelectors.any())
                //为当前包下controller生成API文档
//                .apis(RequestHandlerSelectors.basePackage(project.packageName+".controller"))
                .paths(PathSelectors.any())
                .build();

        //赋予插件体系
        docket.extensions(openApiExtensionResolver.buildExtensions(GROUP_NAME))
                //设置安全模式，添加登录认证
                .securitySchemes(securitySchemes())
                .securityContexts(securityContexts());

        return docket;
    }

    /**
     * 将api的元信息设置为包含在json resource listing响应中
     * @return
     */
    private ApiInfo apiInfo() {
        Contact contact = new Contact(
                author.name,
                author.github,
                author.email);
        //设置文档信息
        return new ApiInfoBuilder()
                .title(project.name+"api文档")
                .description(project.desc)
                .contact(contact)
                .version(project.version)
                .license(project.license)//更新此API的许可证信息
                .licenseUrl(project.licenseUrl)//更新此API的许可证Url
                .termsOfServiceUrl(project.termsOfServiceUrl)//更新服务条款URL
                .build();
    }


    /**
     * 给API文档接口添加安全认证
     * 安全模式，这里指定token通过Authorization头请求头传递
     * @return
     */
    private List<SecurityScheme> securitySchemes() {
        List<SecurityScheme> securitySchemes = new ArrayList<>();
        //设置请求头信息
        ApiKey apiKey = new ApiKey("Authorization", "Authorization", "header");
        securitySchemes.add(apiKey);
        return securitySchemes;
    }

    private List<SecurityContext> securityContexts() {
        //设置需要登录认证的路径
        List<SecurityContext> result = new ArrayList<>();
        result.add(getContextByPath("^(?!auth).*$"));
        return result;
    }

    private SecurityContext getContextByPath(String pathRegex) {
        return SecurityContext.builder()
                .securityReferences(defaultAuth())
                .forPaths(PathSelectors.regex(pathRegex))
                .build();
    }

    private List<SecurityReference> defaultAuth() {
        List<SecurityReference> result = new ArrayList<>();
        AuthorizationScope authorizationScope = new AuthorizationScope
                ("global", "accessEverything");
        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
        authorizationScopes[0] = authorizationScope;
        result.add(new SecurityReference("Authorization", authorizationScopes));
        return result;
    }

}
