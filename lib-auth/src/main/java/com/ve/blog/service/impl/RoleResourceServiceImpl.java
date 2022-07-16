package com.ve.blog.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ve.blog.dao.RoleResourceDao;
import com.ve.blog.entity.RoleResource;
import com.ve.blog.service.RoleResourceService;
import org.springframework.stereotype.Service;

/**
 * 角色资源服务
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@DS("auth")
@Service
public class RoleResourceServiceImpl extends ServiceImpl<RoleResourceDao, RoleResource> implements RoleResourceService {


}
