package com.ve.blog.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.ve.blog.constant.CommonConst;
import com.ve.blog.dao.UserAuthDao;
import com.ve.blog.dao.UserInfoDao;
import com.ve.blog.dao.UserRoleDao;
import com.ve.blog.dto.SocialUserInfoDTO;
import com.ve.blog.dto.SocialTokenDTO;
import com.ve.blog.dto.UserDetailDTO;
import com.ve.blog.dto.UserInfoDTO;
import com.ve.blog.entity.UserAuth;
import com.ve.blog.entity.UserInfo;
import com.ve.blog.entity.UserRole;
import com.ve.blog.enums.RoleEnum;
import com.ve.blog.exception.BizException;
import com.ve.blog.service.impl.UserDetailsServiceImpl;
import com.ve.blog.strategy.SocialLoginStrategy;
import com.ve.blog.util.BeanCopyUtils;
import com.ve.blog.util.IpUtils;
import com.ve.blog.enums.ZoneEnum;
import com.ve.blog.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

import static com.ve.blog.constant.CommonConst.TRUE;


/**
 * 第三方登录抽象模板
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@Service
public abstract class AbstractSocialLoginStrategyImpl implements SocialLoginStrategy {
    @Autowired
    protected UserAuthDao userAuthDao;
    @Autowired
    protected UserInfoDao userInfoDao;
    @Autowired
    protected UserRoleDao userRoleDao;
    @Autowired
    protected UserDetailsServiceImpl userDetailsService;
    @Resource
    protected HttpServletRequest request;

    @Override
    public UserInfoDTO login(String data) {
        // 创建登录信息
        UserDetailDTO userDetailDTO;
        // 获取第三方token信息
        SocialTokenDTO socialToken = getSocialToken(data);
        // 获取用户ip信息
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        // 判断是否已注册
        UserAuth user = getUserAuth(socialToken);
        if (Objects.nonNull(user)) {
            // 返回数据库用户信息
            userDetailDTO = getUserDetail(user, ipAddress, ipSource);
        } else {
            // 获取第三方用户信息，保存到数据库返回
            userDetailDTO = saveUserDetail(socialToken, ipAddress, ipSource);
        }
        // 判断账号是否禁用
        if (userDetailDTO.getIsDisable().equals(TRUE)) {
            throw new BizException("账号已被禁用");
        }
        // 将登录信息放入springSecurity管理
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetailDTO, null, userDetailDTO.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        // 返回用户信息
        return BeanCopyUtils.copyObject(userDetailDTO, UserInfoDTO.class);
    }

    public UserInfoDTO register(String data){
        // 创建登录信息
        UserDetailDTO userDetailDTO;
        // 获取第三方token信息
        SocialTokenDTO socialToken = getSocialToken(data);
        // 判断是否已注册
        UserAuth user = getUserAuth(socialToken);
        if (!Objects.nonNull(user)){
            throw new BizException("账号已经注册！");
        }
        // 获取用户ip信息
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        // 获取第三方用户信息，保存到数据库返回
        userDetailDTO = saveUserDetail(socialToken, ipAddress, ipSource);
        // 将登录信息放入springSecurity管理
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetailDTO, null, userDetailDTO.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        // 返回用户信息
        return BeanCopyUtils.copyObject(userDetailDTO, UserInfoDTO.class);
    }


    /**
     * 获取第三方token信息
     *
     * @param data 数据
     * @return {@link SocialTokenDTO} 第三方token信息
     */
    public abstract SocialTokenDTO getSocialToken(String data);

    /**
     * 获取第三方用户信息
     *
     * @param socialTokenDTO 第三方token信息
     * @return {@link SocialUserInfoDTO} 第三方用户信息
     */
    public abstract SocialUserInfoDTO getSocialUserInfo(SocialTokenDTO socialTokenDTO);

    /**
     * 获取用户账号
     *
     * @return {@link UserAuth} 用户账号
     */
    protected UserAuth getUserAuth(SocialTokenDTO socialTokenDTO) {
        return userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getUsername, socialTokenDTO.getOpenId())
                .eq(UserAuth::getLoginType, socialTokenDTO.getLoginType()));
    }

    /**
     * 获取用户信息
     *
     * @param user      用户账号
     * @param ipAddress ip地址
     * @param ipSource  ip源
     * @return {@link UserDetailDTO} 用户信息
     */
    protected UserDetailDTO getUserDetail(UserAuth user, String ipAddress, String ipSource) {
        // 更新登录信息
        userAuthDao.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getLastLoginTime, LocalDateTime.now())
                .set(UserAuth::getIpAddress, ipAddress)
                .set(UserAuth::getIpSource, ipSource)
                .eq(UserAuth::getId, user.getId()));
        // 封装信息
        return userDetailsService.convertUserDetail(user, request);
    }

    /**
     * 新增用户信息
     *
     * @param socialToken token信息
     * @param ipAddress   ip地址
     * @param ipSource    ip源
     * @return {@link UserDetailDTO} 用户信息
     */
    protected UserDetailDTO saveUserDetail(SocialTokenDTO socialToken, String ipAddress, String ipSource) {
        // 获取第三方用户信息，Android 无法通过此处获取用户信息，返回null
        SocialUserInfoDTO socialUserInfo = getSocialUserInfo(socialToken);
        LogUtil.println(socialUserInfo);

        String nickname= CommonConst.DEFAULT_NICKNAME + IdWorker.getId();
        String avatar=CommonConst.DEFAULT_AVATAR;
        if(socialUserInfo!=null){
            nickname=socialUserInfo.getNickname();
            avatar=socialUserInfo.getAvatar();
        }
        // 保存用户信息
        UserInfo userInfo = UserInfo.builder()
                .nickname(nickname)
                .avatar(avatar)
                .build();
        userInfoDao.insert(userInfo);


        // 保存账号信息
        UserAuth userAuth = UserAuth.builder()
                .userInfoId(userInfo.getId())
                .username(socialToken.getOpenId())
                .password(socialToken.getAccessToken())
                .loginType(socialToken.getLoginType())
                .lastLoginTime(LocalDateTime.now(ZoneId.of(ZoneEnum.SHANGHAI.getZone())))
                .ipAddress(ipAddress)
                .ipSource(ipSource)
                .build();
        userAuthDao.insert(userAuth);
        // 绑定角色
        UserRole userRole = UserRole.builder()
                .userId(userInfo.getId())
                .roleId(RoleEnum.USER.getRoleId())
                .build();
        userRoleDao.insert(userRole);
        return userDetailsService.convertUserDetail(userAuth, request);
    }

}
