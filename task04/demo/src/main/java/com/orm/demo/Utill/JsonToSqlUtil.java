package com.orm.demo.Utill;

import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.orm.demo.Utill.HttpClientUtil.doGet;
import static com.orm.demo.Utill.HttpClientUtil.doPost;
import static com.orm.demo.Utill.HttpClientUtil.doPostWithEntity;

/**
 * Created by Admin on 2021/3/31.
 */
@Component
public class JsonToSqlUtill {
    @Value("${JsonToSqlUtill.field}")
    private Set<Object> field;

    private static Set<Object> s_field;

    @Value("${JsonToSqlUtill.timeField}")
    private Set<Object> timeField;

    private static Set<Object> s_timeField;

    @Value("${JsonToSqlUtill.timeFieldType}")
    private boolean timeFieldType;

    private static boolean s_timeFieldType;

    @Value("${JsonToSqlUtill.tableName}")
    private String tableName;

    private static String s_tableName;

    @Value("${JsonToSqlUtill.dataPath}")
    private String dataPath;

    private static String s_dataPath;

    @Value("${JsonToSqlUtill.url}")
    private String url;

    private static String s_url;

    @PostConstruct
    public void getField(){
        s_field = this.field;
    }

    @PostConstruct
    public void getTimeField(){
        s_timeField = this.timeField;
    }

    @PostConstruct
    public void getTimeFieldType(){
        s_timeFieldType = this.timeFieldType;
    }

    @PostConstruct
    public void getTableName(){
        s_tableName = this.tableName;
    }
    @PostConstruct
    public void getDataPath(){
        s_dataPath = this.dataPath;
    }
    @PostConstruct
    public void getUrl(){
        s_url = this.url;
    }

    public static String getSqlWithJson(String json) throws IOException {
        //获取httpclient返回数据
        String httpResponse = doPostWithEntity(s_url,json);
        return getSql(httpResponse);
    }

    public static String getSqlWithGet() throws IOException {
        //获取httpclient返回数据
        String httpResponse = doGet(s_url);
        return getSql(httpResponse);
    }

    public static String getSqlWithPost() throws IOException {
        //获取httpclient返回数据
        String httpResponse = doPost(s_url);
        return getSql(httpResponse);
    }

    private static String getSql(String httpResponse) throws IOException {
        List<Object> list = new ArrayList<>();
        //获取json中所需要的数据
        List<HashMap> data = JsonPath.read( httpResponse,"$."+ s_dataPath +"[*]");


        //ON DUPLICATE KEY UPDATE（sql：如果数据库存在就更新，不存在就插入） 后的动态数据
        StringBuilder updateFields = new StringBuilder();
        for (Object key : s_field){
            //顺便将字段加入到list集合中，方便后面的字段判空对比
            list.add(key.toString());
            updateFields.append(key).append("=").append("values(").append(key).append("),");
        }
        //去除最后的逗号
        updateFields.delete(updateFields.length()-1,updateFields.length()).append(";");

        //获取values里要插入的字段
        //索引，用作判断循环中的map中，某个字段存在空值，就用null代替
        int index = 0;
        StringBuilder values = new StringBuilder();
        for (Map map : data){
            //每次循环置0
            index = 0;
            values.append("(");
            for (Object key : map.keySet()){
                //发现索引不对，则自动给缺失的数据段填充null值，直到index相等为止
                while (list.indexOf(key) != index){
                    values.append("'").append("null").append("'").append(",");
                    index++;
                }
                //如果值是时间，需要进行格式转换
                if (s_timeField.contains(key)){
                    if (s_timeFieldType){
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                        String date = df.format(new Date(Long.parseLong((map.get(key).toString()))));
                        //写入时间，跳过后面的执行步骤
                        values.append("'").append(date).append("'").append(",");
                    }else {
                        values.append("'").append(map.get(key).toString()).append("'").append(",");
                    }
                    index++;
                    continue;
                }

                //将值加入到values中
                values.append("'").append(map.get(key)).append("'").append(",");
                //进入到下一个索引中
                index++;
            }
            //最后在给末尾缺失的数据段填充null值
            while (list.size() != index){
                //如果缺少的是时间，取默认时间
                if (s_timeField.contains(list.get(index))){
                    //写入时间，跳过后面的执行步骤
                    values.append("'").append("0000-00-00").append("'").append(",");
                    index++;
                    continue;
                }
                values.append("'").append("null").append("'").append(",");
                index++;
            }
            //去除最后一个字段的逗号
            values.delete(values.length()-1,values.length());
            values.append("),");
        }
        //去除生成在末尾的逗号
        values.delete(values.length()-1,values.length());

        //最后的拼接
        StringBuilder finallySql = new StringBuilder();
        finallySql.append("INSERT INTO ").append(s_tableName).append(" (").append(s_field.toString().substring(1,s_field.toString().length()-1)).append(")").
                append("VALUES").append(values).append("ON DUPLICATE KEY UPDATE ").append(updateFields);

        return String.valueOf(finallySql);
    }

}
