package com.ve.blog.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description jwtToken工具栏
 * 用于出token中取出用户登录的信息
 * token的claim里中的字段
 * iss：Issuer，发行者    -- token的发行者
 * sub：Subject，主题     -- 该JWT所面向的用户
 * aud：Audience，观众    -- 接收该JWT的一方
 * exp：Expiration time  -- token的失效时间
 * nbf：Not before       -- 在此时间段之前,不会被处理
 * iat：Issued at Time   -- jwt发布时间
 * jti：JWT ID           -- jwt唯一标识,防止重复使用
 * @Author weiyi
 * @Date 2021/12/27
 * {
 *   "data": {
 *     "type": "token",
 *     "id": "1",
 *     "attributes": {
 *       "token_type": "Bearer",
 *       "expires_in": 2592000,
 *       "access_token": "eyJ0eXAiOiJKV1Qi......dj3H9CCSPib6MQtnaT6VNrw",
 *       "refresh_token": "def50200a26b6a9......10ccbf3c1694084c2d2d276"
 *     }
 *   }
 * }
 */
@Component
public class JwtTokenUtil {

    private static final String issuer = "iLocker";
    private static final String CLAIM_KEY_CONTENT = "content";
    private static final String CLAIM_KEY_PUBLIC_KEY = "publicKey";
    private static final String CLAIM_KEY_PRIVATE_KEY = "privateKey";

    private static final String content = "床前明月光，疑是地上霜。";
    private static String publicKey = "publicKey";
    private static String privateKey = "privateKey";

    @Value("${jwt.tokenHeader}")
    public String tokenHeader;

    @Value("${jwt.tokenHead}")
    public String tokenHead;

    @Value("${jwt.secret}")//jwt 密钥

    public String secret;
    @Value("${jwt.expiration}")//失效时间
    public Long expiration;

    /**
     * 根据用户信息生成token
     */
    //用户信息通过Security中的 UserDetails 拿取
    public String generateToken(String username) {
        //publicKey=keyService.getPublicKey(UserUtils.getUserId());
        //privateKey=keyService.getPrivateKey(UserUtils.getUserId());
        //从数据库中查出公钥和私钥
        Map<String, Object> jwtToken = new HashMap<>();
        jwtToken.put(Claims.SUBJECT, username);
        jwtToken.put(CLAIM_KEY_CONTENT, content);
        jwtToken.put(CLAIM_KEY_PUBLIC_KEY, publicKey);
        jwtToken.put(CLAIM_KEY_PRIVATE_KEY, privateKey);
        //根据荷载生成jwt
        return generateToken(jwtToken);
    }

    /**
     * 根据荷载生成 JWT TOKEN
     * @param claims
     */
    private String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                //设置负载
                .setClaims(claims)
                //发行者
                .setIssuer(issuer)
                //发行时间
                .setIssuedAt(new Date())
                //失效时间
                .setExpiration(generateExpirationDate())
                //签名 设置加密算法，通常为HMAC SHA256
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 生成token失效时间
     */
    private Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    /**
     * 从token中获取登录用户名
     */
    public String getUserNameFromToken(String token) {
        String username;
        try {
            Claims claims = getClaimsFromToken(token);
            //通过荷载 claims 就可以拿到用户名 sub字段
            username = claims.getSubject();
        } catch (Exception e) {
            username = null;
        }
        return username;
    }

    /**
     * 判断token是否有效,即token中用户名是否等于userDetails中的用户名
     * @param userDetails
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = getUserNameFromToken(token);
        //token是否过期并且荷载中的用户名和userDetails中是否一致
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * 判断token是否可以被刷新
     * 如果过期了就可以被刷新，如果没过期就不能被刷新
     */
    public boolean canRefresh(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 刷新token
     */
    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        //将创建时间改成当前时间，就相当于去刷新了
        claims.put(Claims.ISSUED_AT, new Date());
        return generateToken(claims);
    }

    /**
     * 判断token是否失效
     */
    private boolean isTokenExpired(String token) {
        Date expireDate = getExpiredDateFromToken(token);
        //判断token时间是否是当前时间的前面 .before
        return expireDate.before(new Date());
    }

    /**
     * 从token中获取过期时间
     */
    private Date getExpiredDateFromToken(String token) {
        //从token里面获取荷载
        //因为token的过期时间有对应的数据,设置过的,荷载里面就有设置过的数据
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }


    /**
     * 从token中获取荷载
     */
    private Claims getClaimsFromToken(String token) {
        //拿到荷载
        Claims claims = null;
        claims = Jwts.parser()
                //签名
                .setSigningKey(secret)
                //密钥
                .parseClaimsJws(token)
                .getBody();
        return claims;
    }


    public String getUserNameFromToken(HttpServletRequest httpServletRequest) {
        String userName = null;
        //通过 request 获取请求头
        String authHeader = httpServletRequest.getHeader(tokenHeader);
        //验证头部，不存在，或者不是以tokenHead：Bearer开头的
        if (authHeader != null && authHeader.startsWith(tokenHead)) {
            //存在，就做一个字符串的截取，其实就是获取了登录的token
            String authToken = authHeader.substring(tokenHead.length());
            //jwt根据token获取用户名
            //token存在用户名但是未登录
            userName = getUserNameFromToken(authToken);
        }
        return userName;
    }

    public Claims getTokenClaimInfo(HttpServletRequest httpServletRequest) {
        //  Map tokenInfoMap = new HashMap();
        String userName = null;
        //通过 request 获取请求头
        String authHeader = httpServletRequest.getHeader(tokenHeader);
        //验证头部，不存在，或者不是以tokenHead：Bearer开头的
        if (authHeader != null && authHeader.startsWith(tokenHead)) {
            //存在，就做一个字符串的截取，其实就是获取了登录的token
            String authToken = authHeader.substring(tokenHead.length());
            //jwt根据token获取用户名
            //token存在用户名但是未登录
            userName = getUserNameFromToken(authToken);
            return getClaimsFromToken(authToken);
        }
        return null;
    }

    public String getUserPublicKey(HttpServletRequest httpServletRequest) {
        return (String) getTokenClaimInfo(httpServletRequest).get(CLAIM_KEY_PUBLIC_KEY);
    }

    public String getUserPrivateKey(HttpServletRequest httpServletRequest) {
        return (String) getTokenClaimInfo(httpServletRequest).get(CLAIM_KEY_PRIVATE_KEY);
    }
}
