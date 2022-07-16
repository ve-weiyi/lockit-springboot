package com.ve.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ve.blog.constant.CommonConst;
import com.ve.blog.dao.UserInfoDao;
import com.ve.blog.dao.UserRoleDao;
import com.ve.blog.dto.*;
import com.ve.blog.entity.UserInfo;
import com.ve.blog.entity.UserAuth;
import com.ve.blog.dao.UserAuthDao;
import com.ve.blog.entity.UserRole;
import com.ve.blog.enums.LoginTypeEnum;
import com.ve.blog.enums.RoleEnum;
import com.ve.blog.enums.StatusCodeEnum;
import com.ve.blog.exception.BizException;
import com.ve.blog.service.RedisService;
import com.ve.blog.service.UserAuthService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ve.blog.strategy.context.SocialLoginStrategyContext;
import com.ve.blog.util.*;
import com.ve.blog.vo.*;
import com.ve.blog.constant.MQPrefixConst;
import com.ve.blog.constant.RedisPrefixConst;
import com.ve.blog.enums.UserAreaTypeEnum;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/**
 * 用户账号服务
 *
 * @author yezhiqiu
 * @date 2021/08/10
 */
@DS("auth")
@Service
public class UserAuthServiceImpl extends ServiceImpl<UserAuthDao, UserAuth> implements UserAuthService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private UserAuthDao userAuthDao;
    @Autowired
    private UserRoleDao userRoleDao;
    @Autowired
    private UserInfoDao userInfoDao;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SocialLoginStrategyContext socialLoginStrategyContext;


    @Autowired
    private UserDetailsServiceImpl userDetailsService;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    public void sendCode(String username) {
        // 校验账号是否合法
        if (!CommonUtils.checkEmail(username)) {
            throw new BizException("请输入正确邮箱");
        }
        // 生成六位随机验证码发送
        String code = CommonUtils.getRandomCode();
        // 发送验证码
        EmailDTO emailDTO = EmailDTO.builder()
                .email(username)
                .subject("验证码")
                .content("您的验证码为 " + code + " 有效期15分钟，请不要告诉他人哦！")
                .build();
        rabbitTemplate.convertAndSend(MQPrefixConst.EMAIL_EXCHANGE, "*", new Message(JSON.toJSONBytes(emailDTO), new MessageProperties()));
        // 将验证码存入redis，设置过期时间为15分钟
        redisService.set(RedisPrefixConst.USER_CODE_KEY + username, code, RedisPrefixConst.CODE_EXPIRE_TIME);
    }

    @Override
    public List<UserAreaDTO> listUserAreas(ConditionVO conditionVO) {
        List<UserAreaDTO> userAreaDTOList = new ArrayList<>();
        switch (Objects.requireNonNull(UserAreaTypeEnum.getUserAreaType(conditionVO.getType()))) {
            case USER:
                // 查询注册用户区域分布
                Object userArea = redisService.get(RedisPrefixConst.USER_AREA);
                if (Objects.nonNull(userArea)) {
                    userAreaDTOList = JSON.parseObject(userArea.toString(), List.class);
                }
                return userAreaDTOList;
            case VISITOR:
                // 查询游客区域分布
                Map<String, Object> visitorArea = redisService.hGetAll(RedisPrefixConst.VISITOR_AREA);
                if (Objects.nonNull(visitorArea)) {
                    userAreaDTOList = visitorArea.entrySet().stream()
                            .map(item -> UserAreaDTO.builder()
                                    .name(item.getKey())
                                    .value(Long.valueOf(item.getValue().toString()))
                                    .build())
                            .collect(Collectors.toList());
                }
                return userAreaDTOList;
            default:
                break;
        }
        return userAreaDTOList;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void register(UserVO user) {
        // 校验账号是否合法
        if (checkUser(user)) {
            throw new BizException(StatusCodeEnum.USERNAME_EXIST.getCode(),"用户已被注册！");
        }
        // 新增用户信息
        UserInfo userInfo = UserInfo.builder()
                .email(user.getUsername())
                .nickname(CommonConst.DEFAULT_NICKNAME + IdWorker.getId())
                .avatar(CommonConst.DEFAULT_AVATAR)
                .build();
        userInfoDao.insert(userInfo);
        // 绑定用户角色
        UserRole userRole = UserRole.builder()
                .userId(userInfo.getId())
                .roleId(RoleEnum.USER.getRoleId())
                .build();
        userRoleDao.insert(userRole);

        // 新增用户账号
        UserAuth userAuth = UserAuth.builder()
                .userInfoId(userInfo.getId())
                .username(user.getUsername())
                .password(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()))
                .loginType(LoginTypeEnum.EMAIL.getType())
                .build();
        userAuthDao.insert(userAuth);
    }


    @Override
    public void updatePassword(UserVO user) {
        // 校验账号是否合法
        if (!checkUser(user)) {
            throw new BizException("邮箱尚未注册！");
        }
        // 根据用户名修改密码
        userAuthDao.update(new UserAuth(), new LambdaUpdateWrapper<UserAuth>()
                .set(UserAuth::getPassword, BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()))
                .eq(UserAuth::getUsername, user.getUsername()));
    }

    @Override
    public void updateAdminPassword(PasswordVO passwordVO) {
        // 查询旧密码是否正确
        UserAuth user = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .eq(UserAuth::getId, UserUtils.getLoginUser().getId()));
        // 正确则修改密码，错误则提示不正确
        if (Objects.nonNull(user) && BCrypt.checkpw(passwordVO.getOldPassword(), user.getPassword())) {
            UserAuth userAuth = UserAuth.builder()
                    .id(UserUtils.getLoginUser().getId())
                    .password(BCrypt.hashpw(passwordVO.getNewPassword(), BCrypt.gensalt()))
                    .build();
            userAuthDao.updateById(userAuth);
        } else {
            throw new BizException("旧密码不正确");
        }
    }

    @Override
    public PageResult<UserBackDTO> listUserBackDTO(ConditionVO condition) {
        // 获取后台用户数量
        Integer count = userAuthDao.countUser(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 获取后台用户列表
        List<UserBackDTO> userBackDTOList = userAuthDao.listUsers(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(userBackDTOList, count);
    }

    @Override
    public LoginVO login(UserVO user) {
        String username=user.getUsername();
        String password=user.getPassword();
        LogUtil.println("用户名："+user.getUsername());
        LogUtil.println("密码："+user.getPassword());
        //登录
        UserDetailDTO userDetails = userDetailsService.loadUserByUsername(username);

        if (Objects.isNull(userDetails)) {
            throw new BizException("用户名不存在!");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BizException("用户名密码不正确!");
        }
        if (!userDetails.isEnabled()) {
            throw new BizException("账号被禁用，请联系管理员!");
        }
        //更新security登录用户对象,放在全文中
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //生成token
        String token = jwtTokenUtil.generateToken(userDetails.getUsername());

        LoginVO loginVO=LoginVO.builder()
                .token(jwtTokenUtil.tokenHead+" "+token)
                .userInfoDTO(BeanCopyUtils.copyObject(userDetails, UserInfoDTO.class))
                .build();
        return loginVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public LoginVO qqLogin(QQLoginVO qqLoginVO) {
        UserInfoDTO userInfo=socialLoginStrategyContext.executeLoginStrategy(JSON.toJSONString(qqLoginVO), LoginTypeEnum.QQ);
        //生成token
        String token = jwtTokenUtil.generateToken(userInfo.getUsername());
        LoginVO loginVO=LoginVO.builder()
                .token(jwtTokenUtil.tokenHead+" "+token)
                .userInfoDTO(userInfo)
                .build();
        return loginVO;
    }

    @Transactional(rollbackFor = BizException.class)
    @Override
    public LoginVO weiboLogin(WeiboLoginVO weiboLoginVO) {
        UserInfoDTO userInfo=socialLoginStrategyContext.executeLoginStrategy(JSON.toJSONString(weiboLoginVO), LoginTypeEnum.WEIBO);
        //生成token
        String token = jwtTokenUtil.generateToken(userInfo.getUsername());
        LoginVO loginVO=LoginVO.builder()
                .token(jwtTokenUtil.tokenHead+" "+token)
                .userInfoDTO(userInfo)
                .build();
        return loginVO;
    }

    /**
     * 校验用户数据是否合法
     *
     * @param user 用户数据
     * @return 结果
     */
    private Boolean checkUser(UserVO user) {
        if (!user.getCode().equals(redisService.get(RedisPrefixConst.USER_CODE_KEY + user.getUsername()))) {
            throw new BizException("验证码错误！");
        }
        //查询用户名是否存在
        UserAuth userAuth = userAuthDao.selectOne(new LambdaQueryWrapper<UserAuth>()
                .select(UserAuth::getUsername)
                .eq(UserAuth::getUsername, user.getUsername()));
        return Objects.nonNull(userAuth);
    }

    /**
     * 统计用户地区
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void statisticalUserArea() {
        // 统计用户地域分布
        Map<String, Long> userAreaMap = userAuthDao.selectList(new LambdaQueryWrapper<UserAuth>().select(UserAuth::getIpSource))
                .stream()
                .map(item -> {
                    if (StringUtils.isNotBlank(item.getIpSource())) {
                        return item.getIpSource().substring(0, 2)
                                .replaceAll(CommonConst.PROVINCE, "")
                                .replaceAll(CommonConst.CITY, "");
                    }
                    return CommonConst.UNKNOWN;
                })
                .collect(Collectors.groupingBy(item -> item, Collectors.counting()));
        // 转换格式
        List<UserAreaDTO> userAreaList = userAreaMap.entrySet().stream()
                .map(item -> UserAreaDTO.builder()
                        .name(item.getKey())
                        .value(item.getValue())
                        .build())
                .collect(Collectors.toList());
        redisService.set(RedisPrefixConst.USER_AREA, JSON.toJSONString(userAreaList));
    }

}
