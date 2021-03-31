package com.orm.demo.dynamic;

import org.slf4j.Logger;

/**
 * Created by oyhk on 16/5/15.
 * 业务 的终止异常,事务回滚
 */
public class BusinessException extends RuntimeException {

    public String desc;

    public BusinessException(String desc) {
        this.desc = desc;
    }

    public BusinessException(String desc, Logger log) {
        this.desc = desc;
        log.warn(this.desc);
    }
}
