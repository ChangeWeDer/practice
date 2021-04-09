package com.sync.demo.task;

import com.sync.demo.Utill.JsonToSqlUtil;
import com.sync.demo.mapper.SyncMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Admin on 2021/4/9.
 */
@Service
public class SyncTask {

    @Autowired
    SyncMapper syncMapper;


    @Autowired
    JsonToSqlUtil jsonToSqlUtil;

    @Scheduled(cron = "10/10 * * * * ? ")
    public void Task() throws IOException {

        HashMap<String, Object> map = jsonToSqlUtil.GetSQL();
        long startTime = System.currentTimeMillis();
        //先删除api中已经不存在的数据
        if (map.get("deleteSql") != null){
            for (String sql : (List<String>)map.get("deleteSql")){
                syncMapper.executeSql(sql);
            }
        }
        //插入同步数据
        for (String sql : (List<String>)map.get("insertSql")){
            syncMapper.executeSql(sql);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("数据已更新,总共耗时：" + (endTime - startTime) + "ms");
    }


}
