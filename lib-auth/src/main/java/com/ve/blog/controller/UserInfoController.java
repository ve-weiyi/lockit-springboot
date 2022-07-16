package com.ve.blog.controller;


import com.ve.blog.annotation.OptLog;
import com.ve.blog.constant.OptTypeConst;
import com.ve.blog.vo.*;
import com.ve.blog.dto.UserOnlineDTO;
import com.ve.blog.service.UserInfoService;
import com.ve.blog.dto.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;



/**
 * 用户信息控制器
 *
 * @author yezhiqiu
 * @date 2021/07/29
 */
@Api(tags = "用户信息模块")
@RestController
public class UserInfoController {
    @Autowired
    private UserInfoService userInfoService;

    /**
     * 更新用户信息
     *
     * @param userInfoVO 用户信息
     * @return {@link Result <>}
     */
    @ApiOperation(value = "更新用户信息")
    @PutMapping("/user/info")
    public Result<?> updateUserInfo(@Valid @RequestBody UserInfoVO userInfoVO) {
        userInfoService.updateUserInfo(userInfoVO);
        return Result.ok();
    }

    /**
     * 更新用户头像
     *
     * @param file 文件
     * @return {@link Result<String>} 头像地址
     */
    @ApiOperation(value = "更新用户头像")
    @ApiImplicitParam(name = "file", value = "用户头像", required = true, dataType = "MultipartFile")
    @PostMapping("/user/avatar")
    public Result<String> updateUserAvatar(MultipartFile file) {
        return Result.ok(userInfoService.updateUserAvatar(file));
    }

    /**
     * 绑定用户邮箱
     *
     * @param emailVO 邮箱信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "绑定用户邮箱")
    @PostMapping("/user/email")
    public Result<?> saveUserEmail(@Valid @RequestBody EmailVO emailVO) {
        userInfoService.saveUserEmail(emailVO);
        return Result.ok();
    }

    /**
     * 修改用户角色
     *
     * @param userRoleVO 用户角色信息
     * @return {@link Result<>}
     */
    @OptLog(optType = OptTypeConst.UPDATE)
    @ApiOperation(value = "修改用户角色")
    @PutMapping("/admin/user/role")
    public Result<?> updateUserRole(@Valid @RequestBody UserRoleVO userRoleVO) {
        userInfoService.updateUserRole(userRoleVO);
        return Result.ok();
    }

    /**
     * 修改用户禁用状态
     *
     * @param userDisableVO 用户禁用信息
     * @return {@link Result<>}
     */
    @OptLog(optType = OptTypeConst.UPDATE)
    @ApiOperation(value = "修改用户禁用状态")
    @PutMapping("/admin/user/disable")
    public Result<?> updateUserDisable(@Valid @RequestBody UserDisableVO userDisableVO) {
        userInfoService.updateUserDisable(userDisableVO);
        return Result.ok();
    }

    /**
     * 查看在线用户
     *
     * @param conditionVO 条件
     * @return {@link Result< UserOnlineDTO >} 在线用户列表
     */
    @ApiOperation(value = "查看在线用户")
    @GetMapping("/admin/user/online")
    public Result<PageResult<UserOnlineDTO>> listOnlineUsers(ConditionVO conditionVO) {
        return Result.ok(userInfoService.listOnlineUsers(conditionVO));
    }

    /**
     * 下线用户
     *
     * @param userInfoId 用户信息
     * @return {@link Result<>}
     */
    @ApiOperation(value = "下线用户")
    @DeleteMapping("/admin/user/{userInfoId}/online")
    public Result<?> removeOnlineUser(@PathVariable("userInfoId") Integer userInfoId) {
        userInfoService.removeOnlineUser(userInfoId);
        return Result.ok();
    }

}

