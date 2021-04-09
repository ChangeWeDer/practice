package com.sync.demo.Utill;


import com.jayway.jsonpath.JsonPath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by Admin on 2021/3/31.
 */
@Component
public class JsonToSqlUtil {

    @Autowired
    private DeleteUtil deleteUtil;

    @Autowired
    private HttpClientUtil httpClientUtil;

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

    @Value("${JsonToSqlUtil.method}")
    private String method;

    @Value("${JsonToSqlUtil.body}")
    private String body;

    public  HashMap<String,Object> GetSQL() throws IOException {
        if (method.equals("get"))
            return getSqlWithGet();
        else
            return getSqlWithJson();
    }

    public  HashMap<String,Object> getSqlWithJson() throws IOException {
        //获取httpclient返回数据
        String httpResponse = httpClientUtil.doPostWithEntity(url,body);
        return getSql(httpResponse);
    }

    public  HashMap<String,Object> getSqlWithGet() throws IOException {
        //获取httpclient返回数据
        String httpResponse = httpClientUtil.doGet(url);
        return getSql(httpResponse);
    }

    public  HashMap<String,Object> getSqlWithPost() throws IOException {
        //获取httpclient返回数据
        String httpResponse = httpClientUtil.doPost(url);
        return getSql(httpResponse);
    }

    private HashMap<String,Object> getSql(String httpResponse) throws IOException {
        List<Object> list = new ArrayList<>();

        HashMap<String,Object> SqlMap = new HashMap<>();
        //获取所有主键id，通过与数据库的id进行对比，如果减少了，则删除数据库对应的列
        List<Integer> Data = JsonPath.read( httpResponse,"$."+dataPath+"[*]."+ primaryKey);

        //接收返回的SQL List
        List<String> deleteSql = deleteUtil.deleteData(Data);
        if (deleteSql != null)
            SqlMap.put("deleteSql",deleteSql);

        //获取json中所需要的数据
        List<HashMap<String,String>> data = JsonPath.read( httpResponse,"$."+ dataPath +"[*]");

        //ON DUPLICATE KEY UPDATE 后的动态数据
        StringBuilder updateFields = new StringBuilder();
        for (Object key : field){
            //顺便将字段加入到list集合中，方便后面的字段判空对比
            list.add(key.toString());
            updateFields.append(key).append("=").append("values(").append(key).append("),");
        }
        //去除最后的逗号
        updateFields.delete(updateFields.length()-1,updateFields.length()).append(";");

        //获取values里要插入的字段
        //索引，用作判断循环中的map中，某个字段存在空值，就用null代替
        int index = 0;
        //用作sql分段
        int SqlIndex = 0;
        //存储每段的SQL
        List<String> Storage = new LinkedList<>();
        //SQL中的values
        StringBuilder values = new StringBuilder();
        //最后的SQL
        StringBuilder finallySql = new StringBuilder();
        for (Map map : data){
            //每次循环置0,用作判断缺省字段
            index = 0;
            values.append("(");
            for (Object key : map.keySet()){
                //发现索引不对，则自动给缺失的数据段填充null值，直到index相等为止
                while (list.indexOf(key) != index){
                    values.append("'").append("null").append("'").append(",");
                    index++;
                }
                //如果值是时间，需要进行格式转换
                if (timeField.contains(key)){
                    if (timeFieldType){
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
                if (timeField.contains(list.get(index))){
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

            SqlIndex++;
            //当数据量达到5000时分段
            if (SqlIndex / 5000 == 1){
                //去除生成在末尾的逗号
                values.delete(values.length()-1,values.length());
                //最后的拼接
                finallySql.append("INSERT INTO ").append(tableName).append(" (").append(field.toString().substring(1,field.toString().length()-1)).append(")").
                        append("VALUES").append(values).append("ON DUPLICATE KEY UPDATE ").append(updateFields);
                //加入到集合中
                Storage.add(String.valueOf(finallySql));
                //清空
                SqlIndex = 0;
                finallySql.delete(0,finallySql.length());
                values.delete(0,values.length());
            }
        }

        if (SqlIndex != 0){
            //循环结束后，还要加上末尾的数据
            //去除生成在末尾的逗号
            values.delete(values.length()-1,values.length());
            //最后的拼接
            finallySql.append("INSERT INTO ").append(tableName).append(" (").append(field.toString().substring(1,field.toString().length()-1)).append(")").
                    append("VALUES").append(values).append("ON DUPLICATE KEY UPDATE ").append(updateFields);
            //加入到集合中
            Storage.add(String.valueOf(finallySql));
        }

        SqlMap.put("insertSql",Storage);
        return SqlMap;
    }



}
