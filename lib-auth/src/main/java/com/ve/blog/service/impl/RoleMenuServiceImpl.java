package com.ve.blog.service.impl;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ve.blog.dao.RoleMenuDao;
import com.ve.blog.entity.RoleMenu;
import com.ve.blog.service.RoleMenuService;
import org.springframework.stereotype.Service;

/**
 * 角色菜单服务
 *
 * @author yezhiqiu
 * @date 2021/07/28
 */
@DS("auth")
@Service
public class RoleMenuServiceImpl extends ServiceImpl<RoleMenuDao, RoleMenu> implements RoleMenuService {


}
