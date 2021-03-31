package com.orm.demo.repository.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.orm.demo.dynamic.SQLUpdateDoing;
import com.orm.demo.repository.Demo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.orm.demo.Utill.HttpClientUtil.doPostWithEntity;
import static com.orm.demo.Utill.JsonToSqlUtill.getSqlWithGet;
import static com.orm.demo.Utill.JsonToSqlUtill.getSqlWithJson;
import static com.orm.demo.Utill.JsonToSqlUtill.getSqlWithPost;

/**
 * Created by Admin on 2021/3/26.
 */
@Repository
public class DemoImpl implements Demo{
    private static final Logger log = LoggerFactory.getLogger(DemoImpl.class);

    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;




    @Override
    public void insertData(String json) throws IOException {

//        //通过Get方式请求
//        String Sql = getSqlWithGet();
//
//        //通过Post方式请求
//        String Sql = getSqlWithPost();
//        System.out.println(Sql);

        //通过Post+Json参数请求
        String Sql = getSqlWithJson("{\n" +
                "  \"pageNo\": 1,\n" +
                "  \"pageSize\": 10\n" +
                "}");
        System.out.println(Sql);

        SQLUpdateDoing<Object> result = (sql, fields, params, typeReferenceMap) -> {
            sql.append(" %s ");
            //动态SQL
            fields.append(Sql);
        };
        result.go(Object.class, jdbcTemplate, log, objectMapper);
   }


}
