package com.ve.blog.controller;

import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

/**
 * 1.放在controller类上
 * -@ApiSupport(order = 分组排序)
 * -@Api(tags="分组名称",value="该参数没什么意义，所以不需要配置")
 * 
 * 2.放在controller类请求的方法上
 * -@ApiOperationSupport(order = 接口排序) 
 * -@ApiOperation(value="说明方法的作用",notes="方法的备注说明")
 * -@ApiImplicitParams：用在请求的方法上，包含一组参数说明
 *     -@ApiImplicitParam：对单个参数的说明
 *         name：参数名
 *         value：参数的汉字说明、解释
 *         required：参数是否必须传
 *         paramType：参数放在哪个地方
 *             · header --> 请求参数的获取：@RequestHeader
 *             · query --> 请求参数的获取：@RequestParam
 *             · path（用于restful接口）--> 请求参数的获取：@PathVariable
 *             · body（请求体）-->  @RequestBody User user
 *             · form（普通表单提交）
 *         dataType：参数类型，默认String，其它值dataType="int"
 *         defaultValue：参数的默认值
 * -@ApiResponses：方法返回对象的说明
 *     -@ApiResponse：每个参数的说明
 *         code：数字，例如400
 *         message：信息，例如"请求参数没填好"
 *         response：抛出异常的类
 *
 * 3.放在JavaBean的类上面
 * -@ApiModel：用于JavaBean的类上面，表示此 JavaBean 整体的信息
 *     （这种一般用在post创建的时候，使用 @RequestBody 这样的场景，请求参数无法使用 @ApiImplicitParam 注解进行描述的时候 ）
 * -@ApiModelProperty：用在JavaBean的属性上面，说明属性的含义
 *
 * @Author weiyi
 * @Date 2022/2/10
 */

@Api(tags = { "测试分组" })
@ApiSupport(order = 100)
@RestController
@RequestMapping("")
public class TestController {

    @ApiOperationSupport(order = 1)
    @PostMapping("/")
    @ApiOperation(value = "根据name和sex获取用户", notes = "方法的备注说明，如果有可以写在这里", httpMethod = "POST")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userName", value = "用户名", example = "关羽", required = true, dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "userSex", value = "用户性别", example = "男", required = true, dataType = "string", paramType = "query") })
    @ApiResponses({
            @ApiResponse(code = 400, message = "请求参数没填好"),
            @ApiResponse(code = 404, message = "请求路径找不到")
    })
    public String getUserByNameAndSex(String userName, String userSex) {
        return "Hello World";
    }



}