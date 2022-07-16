package com.ve.blog.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ve.blog.enums.ZoneEnum;
import com.ve.blog.util.LogUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * mybatis plus自动填充
 *
 * @author yezhiqiu
 * @date 2021/06/12
 **/
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LogUtil.println("start insert fill ....");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now(ZoneId.of(ZoneEnum.SHANGHAI.getZone())));
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        LogUtil.println("start update fill ....");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now(ZoneId.of(ZoneEnum.SHANGHAI.getZone())));
    }

}