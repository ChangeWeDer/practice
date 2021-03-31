package com.orm.demo.dynamic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by oyhk on 16/2/18.
 */
public interface BaseDoing<T> {

    default void showRequestBody(Object body, ObjectMapper objectMapper, Logger log) throws JsonProcessingException {
        if (body != null && objectMapper != null) {
            log.info("======================>request body {}", objectMapper.writeValueAsString(body));
        }
    }

    /**
     * 请求参数记录
     *
     * @param request
     * @param log
     */
    default void showParams(HttpServletRequest request, Logger log) throws IOException {
        if (request != null) {
            Enumeration<String> parameterNames = request.getParameterNames();
            StringBuffer parameterStringBuffer = new StringBuffer("request params => { ");
            while (parameterNames.hasMoreElements()) {
                String parameterName = parameterNames.nextElement();
                parameterStringBuffer.append(parameterName).append(" : ").append(request.getParameter(parameterName)).append(" ,");
            }
            parameterStringBuffer.deleteCharAt(parameterStringBuffer.length() - 1);

            parameterStringBuffer.append(" }");
            log.info(parameterStringBuffer.toString());
        }
    }

    default void showErrorLog(Exception e, Logger log) {
        this.showErrorLog(null, e, log);
    }

    default void showErrorLog(JsonResult jr, Exception e, Logger log) {
        if (jr != null) {
            jr.saveResult(jr.CD_0, log);
        }
        log.error(e.getMessage(), e);
    }

    default void showReturnData(JsonResult jr, ObjectMapper objectMapper, Logger log) {
        if (objectMapper != null) {
            try {
                String returnData = objectMapper.writeValueAsString(jr);
                log.warn("======================>return data : {}", returnData);
            } catch (JsonProcessingException e) {
                log.error(e.getMessage(), e);
            }
        }
    }


    default void showVisitor(Object merchantDto, ObjectMapper objectMapper, Logger log) throws JsonProcessingException {
        if (objectMapper != null && merchantDto != null) {
            log.info("======================>visitor {} ", objectMapper.writeValueAsString(merchantDto));
        }
    }

    /**
     * 错误日志输出
     *
     * @param log
     * @param e
     * @param viewData
     */
    default void errorLog(Logger log, Exception e, ViewData viewData) {
        viewData.view = "/error";
        this.errorLog(log, e);
    }

    /**
     * 错误日志输出
     *
     * @param log
     * @param e
     * @param jr
     */
    default void errorLog(Logger log, Exception e, JsonResult<T> jr) {
        jr.saveResult(jr.CD_0, log);
        this.errorLog(log, e);
    }

    default void errorLog(Logger log, Exception e) {
        log.error(e.getMessage(), e);
    }

    default void showSql(Map<String, Object> params, String sqlName, String sql, Logger log) {
        // 输出 sql
        Set<String> paramsKeys = params.keySet();
        Iterator<String> keys = paramsKeys.iterator();
        while (keys.hasNext()) {
            String key = keys.next();
//            log.info("======================>key:{}", key);
            sql = sql.replace(":" + key, params.get(key).toString());
        }

        log.info("======================>{} : {}", sqlName, sql);
    }


}
