package com.ve.blog.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ve.blog.dao.UserRoleDao;
import com.ve.blog.entity.UserRole;
import com.ve.blog.service.UserRoleService;
import org.springframework.stereotype.Service;


/**
 * 用户角色服务
 *
 * @author yezhiqiu
 * @date 2021/08/10
 */
@DS("auth")
@Service
public class UserRoleServiceImpl extends ServiceImpl<UserRoleDao, UserRole> implements UserRoleService {


}
