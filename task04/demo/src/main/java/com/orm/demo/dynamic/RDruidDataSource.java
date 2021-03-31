package com.orm.demo.dynamic;

import com.alibaba.druid.pool.DruidDataSource;

/**
 * Created by oyhk on 16/7/25.
 * 数据库连接池 暂时简单 创建
 */
public class RDruidDataSource extends DruidDataSource {

    private RDruidDataSource() {
    }

    public static DruidDataSource getInstance(String jdbcUrl, String jdbcUsername, String jdbcPassword) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setMaxActive(100);
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(jdbcUsername);
        dataSource.setPassword(jdbcPassword);
        return dataSource;
    }
}
