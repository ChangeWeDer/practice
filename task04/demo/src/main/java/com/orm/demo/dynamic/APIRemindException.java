package com.orm.demo.dynamic;

/**
 * Created by oyhk on 16/5/15.
 * API 的终止异常,事务回滚
 */
public class APIRemindException extends RuntimeException {

    public String desc;

    public APIRemindException(String desc) {
        this.desc = desc;
    }
}
