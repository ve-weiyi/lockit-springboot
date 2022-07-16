package com.ve.blog;


import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.TrustStrategy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Bean;

import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

/**
 * 博客启动类
 *
 * @author yezhiqiu
 * @date 2021/08/14
 */
@MapperScan(value = {"com.ve.blog.dao","com.ve.blog.mapper"})
@SpringBootApplication
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
//        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    /**
     * RestTemplate是Spring提供的用于访问Rest服务的客户端，RestTemplate提供了多种便捷访问远程Http服务的方法,能够大大提高客户端的编写效率。
     * @return
     */
    @Bean
    public RestTemplate restTemplate() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;

        SSLContext sslContext = org.apache.http.ssl.SSLContexts.custom()
                .loadTrustMaterial(null, acceptingTrustStrategy)
                .build();

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(csf)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory =
                new HttpComponentsClientHttpRequestFactory();

        requestFactory.setHttpClient(httpClient);
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        return restTemplate;
    }

}
