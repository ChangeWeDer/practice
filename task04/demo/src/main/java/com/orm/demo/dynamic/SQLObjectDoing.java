package com.orm.demo.dynamic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oyhk on 16/5/15.
 * 实体查询
 */
public interface SQLObjectDoing<T> extends BaseDoing<T> {

    default T go(Class clazz, NamedParameterJdbcTemplate jdbcTemplate, Logger log, ObjectMapper objectMapper) {
        Long startTime = System.currentTimeMillis();
        String className = "";
        String methodName = "";
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            className = stackTraceElements[2].getFileName();
            methodName = stackTraceElements[2].getMethodName();
        } catch (Exception e) {
        }
        StringBuffer sql = new StringBuffer();
        StringBuffer fields = new StringBuffer();
        Map<String, Object> params = new HashMap<>();
        T object = null;

        // 数据库对应字段 字符串json 转换 对象
        Map<String, TypeReference> typeReferenceMap = new HashMap<>();
        try {

            this.service(sql, fields, params, typeReferenceMap);
            // 当如果没有自定义需要显示的字段,默认是 *
            if (fields.length() <= 0) {
                fields.append("*");
            }
            String dataSql = sql.toString().replace("%s",fields);
//            if (fields.length() >= 1) {
//                dataSql = String.format(sql.toString(), fields);
//            }
            this.showSql(params, "dataSql", dataSql, log);

            // 这个到时候需要扩展一下返回结果的数据类型
            if (clazz == Long.class || clazz == Double.class || clazz == Float.class || clazz == Integer.class) {
               List<T> list =  jdbcTemplate.queryForList(dataSql,params,clazz);
                object = list != null && list.size() != 0 ? list.get(0) : null;
//                object = (T) jdbcTemplate.queryForList(dataSql,params,clazz);
                //queryForObject(dataSql, params, clazz);
            } else {
                RowMapper rowMapper = new BeanPropertyRowMapper<>(clazz);
                if (typeReferenceMap.size() > 0) {
                    rowMapper = new RBeanPropertyRowMapper<T>(clazz, objectMapper, typeReferenceMap);
                }
                List<T> list = jdbcTemplate.query(dataSql, params, rowMapper);
                object = list != null && list.size() != 0 ? list.get(0) : null;
            }

        } catch (IncorrectResultSetColumnCountException e) { // 证明id 对应的记录为空
            this.showErrorLog(e, log);
        } catch (Exception e) {
            this.showErrorLog(e, log);
        } finally {
            long spendTime = System.currentTimeMillis() - startTime;
            if(spendTime>3000) {
                log.error("className:{}, method:{}, processing time {} ms", className, methodName, spendTime);
            } else if(spendTime>1000){
                log.warn("className:{}, method:{}, processing time {} ms", className, methodName, spendTime);
            } else {
                log.info("className:{}, method:{}, processing time {} ms", className, methodName, spendTime);
            }
        }
        return object;
    }

    void service(StringBuffer sql, StringBuffer fields, Map<String, Object> params, Map<String, TypeReference> typeReferenceMap) throws ParseException;
}
