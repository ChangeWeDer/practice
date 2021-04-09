package com.sync.demo.Utill;

import com.sync.demo.mapper.SyncMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Created by Admin on 2021/4/6.
 */
@Component
public class DeleteUtil {

    @Autowired
    private SyncMapper syncMapper;

    @Value("${JsonToSqlUtil.field}")
    private Set<Object> field;


    @Value("${JsonToSqlUtil.timeField}")
    private Set<Object> timeField;


    @Value("${JsonToSqlUtil.timeFieldType}")
    private boolean timeFieldType;


    @Value("${JsonToSqlUtil.tableName}")
    private String tableName;


    @Value("${JsonToSqlUtil.primaryKey}")
    private String primaryKey;


    @Value("${JsonToSqlUtil.dataPath}")
    private String dataPath;


    @Value("${JsonToSqlUtil.url}")
    private String url;

    

    public  List<String> deleteData(List<Integer> data){
        List<String> list = new LinkedList<>();
        //获取数据库中的数据
        Set<Integer> all = syncMapper.getListId(" select "+primaryKey+" from "+tableName);
        List<Integer> resultList = new LinkedList<>();
        Set<Integer> destinationSet = new HashSet<>(data);
        //采用set集合继续数据对比
        for (Integer key : all){
            if (!destinationSet.contains(key))
                resultList.add(key);
        }
        if (resultList.isEmpty()){
            return null;
        }

        //SQL分段
        int count = 0;
        StringBuilder sql = new StringBuilder();
        List<Integer> lastList = new LinkedList<>();
        for (Integer index : resultList){
            lastList.add(index);
            count++;
            //达到5000时分段
            if (count / 5000 == 1 ){
                sql.append("DELETE FROM ").append(tableName).append(" WHERE ").
                        append(primaryKey).append(" in ").append("(").
                        append(lastList.toString().substring(1,lastList.toString().length()-1)).append(");");
                list.add(String.valueOf(sql));

                //清空
                count = 0;
                lastList.clear();
                sql.delete(0,sql.length());
            }
        }
        if (count != 0){
            sql.append("DELETE FROM ").append(tableName).append(" WHERE ").
                    append(primaryKey).append(" in ").append("(").
                    append(lastList.toString().substring(1,lastList.toString().length()-1)).append(");");
            list.add(String.valueOf(sql));
        }

        return list;
    }

}
