package com.orm.demo.dynamic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.springframework.jdbc.IncorrectResultSetColumnCountException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by oyhk on 16/5/15.
 * 列表查询
 */
public interface SQLListDoing<T> extends BaseDoing<T> {

    default List<T> go(Class clazz, NamedParameterJdbcTemplate jdbcTemplate, Logger log, ObjectMapper objectMapper) {
        Long startTime = System.currentTimeMillis();
        String className = "";
        String methodName = "";
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            className = stackTraceElements[3].getFileName();
            methodName = stackTraceElements[3].getMethodName();
        } catch (Exception e) {
        }
        List<T> result = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        StringBuffer fields = new StringBuffer();
        Map<String, Object> params = new HashMap<>();

        // 数据库对应字段 字符串json 转换 对象
        Map<String, TypeReference> typeReferenceMap = new HashMap<>();
        try {

            this.service(sql, fields, params, typeReferenceMap);
            String dataSql = sql.toString();
            if (fields.length() >= 1) {
                // dataSql = String.format(sql.toString(), fields);
                dataSql = dataSql.replace("%s", fields);
            }
            this.showSql(params, "dataSql", dataSql, log);
            if (clazz == Long.class || clazz == Double.class || clazz == Float.class || clazz == Integer.class) {
                result = jdbcTemplate.queryForList(dataSql, params, clazz);
            } else {
                RowMapper rowMapper = new BeanPropertyRowMapper<>(clazz);
                if (typeReferenceMap.size() > 0) {
                    rowMapper = new RBeanPropertyRowMapper<T>(clazz, objectMapper, typeReferenceMap);
                }
                result = jdbcTemplate.query(dataSql, params, rowMapper);
            }

        } catch (IncorrectResultSetColumnCountException e) { // 证明id 对应的记录为空

        } catch (Exception e) {
            this.showErrorLog(e, log);
        } finally {
            long spendTime = System.currentTimeMillis() - startTime;
            if(spendTime > 3000) {
                log.error("className:{}, method:{}, processing time {} ms", className, methodName, spendTime);
            } else if(spendTime>1000) {
                log.warn("className:{}, method:{}, processing time {} ms", className, methodName, spendTime);
            } else {
                log.info("className:{}, method:{}, processing time {} ms", className, methodName, spendTime);
            }
        }
        return result;
    }

    /**
     * 调转参数位置,统一一下
     * @param clazz
     * @param objectMapper
     * @param jdbcTemplate
     * @param log
     * @return
     */
    default List<T> go(Class clazz, ObjectMapper objectMapper, NamedParameterJdbcTemplate jdbcTemplate, Logger log) {
        return this.go(clazz, jdbcTemplate, log, objectMapper);
    }

    void service(StringBuffer sql, StringBuffer fields, Map<String, Object> params, Map<String, TypeReference> typeReferenceMap) throws ParseException;
}
