package org.cc.ua.exception;

import org.cc.common.exception.GlobalException;

/**
 * @ClassName: MissingOptPermission
 * @Description: TODO
 * @Author: CC
 * @Date 2021/5/14 17:48
 * @ModifyRecords: v1.0 new
 */
public class MissingOptPermission extends GlobalException {
    public MissingOptPermission() {
        super(501, "无操作目标数据权限");
    }
}
