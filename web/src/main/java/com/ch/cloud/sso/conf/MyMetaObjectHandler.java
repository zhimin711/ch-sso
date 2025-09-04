package com.ch.cloud.sso.conf;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.ch.toolkit.ContextUtil;
import com.ch.utils.DateUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * <p>
 * desc: MyMetaObjectHandler
 * </p>
 *
 * @author zhimin
 * @since 2025/6/16 09:35
 */
@Component
public class MyMetaObjectHandler  implements MetaObjectHandler {
    
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createAt", Date.class, DateUtils.current());
        this.strictInsertFill(metaObject, "createTime", Date.class, DateUtils.current());
        this.strictInsertFill(metaObject, "createBy", String.class, ContextUtil.getUsername());
//        this.strictInsertFill(metaObject, "updateAt", Date.class, new Date());
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateAt", Date.class, new Date());
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        this.strictUpdateFill(metaObject, "updateBy", String.class, ContextUtil.getUsername());
    }
    
}
