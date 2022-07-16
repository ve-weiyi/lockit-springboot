package com.ve.blog.vo;

import com.ve.blog.dto.UserInfoDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * QQ登录
 *
 * @author yezhqiu
 * @date 2021/06/14
 * @since 1.0.0
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "登录返回信息")
public class LoginVO {

    /**
     * accessToken
     */
    @NotBlank(message = "accessToken不能为空")
    @ApiModelProperty(name = "token", value = " ", required = true, dataType = "String")
    private String token;


    @NotBlank(message = "UserInfoDTO不能为空")
    @ApiModelProperty(name = "userInfoDTO", value = " ", required = true, dataType = "UserInfoDTO")
    private UserInfoDTO userInfoDTO;
}
