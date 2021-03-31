package com.orm.demo.dynamic;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by oyhk on 16/2/18.
 */
@FunctionalInterface
public interface RestDoing<T> extends BaseDoing<T> {
    final Logger log = LoggerFactory.getLogger(RestDoing.class);

    default JsonResult<T> go(Logger log) {
        return this.invoke(null, null, null, null, log);
    }

    default JsonResult<T> go(HttpServletRequest r, Logger log) {
        return this.invoke(null, null, r, null, log);
    }

    default JsonResult<T> go(HttpServletRequest r, ObjectMapper objectMapper, Logger log) {
        return this.invoke(null, null, r, objectMapper, log);
    }

    default JsonResult<T> go(Object visitor, HttpServletRequest request, ObjectMapper objectMapper, Logger log) {
        return this.invoke(null, visitor, request, objectMapper, log);
    }

    default JsonResult<T> go(Object inputData, Object visitor, HttpServletRequest request, ObjectMapper objectMapper, Logger log) {
        return this.invoke(inputData, visitor, request, objectMapper, log);
    }

    default JsonResult<T> invoke(Object inputData, Object visitor, HttpServletRequest request, ObjectMapper objectMapper, Logger log) {
        Long startTime = System.currentTimeMillis();
        String methodName = Thread.currentThread().getStackTrace()[3].getMethodName();
        log.info("======================>current run method: {} <====================== ", methodName);
        JsonResult<T> jr = new JsonResult();
        try {
            this.showVisitor(visitor, objectMapper, log);
            this.showParams(request, log);
            if (objectMapper != null && inputData != null) {
                log.info("======================>request body  {}", objectMapper.writeValueAsString(inputData));
            }
            // 验证 参数的合法性
//            if (inputData != null && validator != null) {
//                BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(inputData, "inputData");
//                validator.validate(inputData, bindingResult);
//                if (bindingResult.hasErrors()) {
//                    jr.errorParam(bindingResult.getAllErrors().get(0).getDefaultMessage());
//                    return jr;
//                }
//            }

            this.service(jr);
        } catch (APIErrorParamsException e) {
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (NoTransactionException e1) {
            }
            jr.errorParam(e.desc, log);
        } catch (APIRemindException e) {
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (NoTransactionException e1) {
            }
            jr.errorParam(e.desc, log);
        } catch (BusinessException e) {
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (NoTransactionException e1) {
            }
            jr.errorParam(e.desc, log);
        } catch (Exception e) {
            try {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            } catch (NoTransactionException e1) {
            }
            this.errorLog(log, e, jr);
//            jr.code = JsonResult.CD0[0];
//            jr.desc = JsonResult.CD0[1];
            jr.saveResult(jr.CD_0,log);
        } finally {
            this.showReturnData(jr, objectMapper, log);
            long spendTime = System.currentTimeMillis() - startTime;
            if (spendTime > 300) {
                log.error("======================>current run method:{} , processing time {} ms <======================", methodName, spendTime);
            } else if (spendTime > 150) {
                log.warn("======================>current run method:{} , processing time {} ms <======================", methodName, spendTime);
            } else {
                log.info("======================>current run method:{} , processing time {} ms <======================", methodName, spendTime);
            }

        }
        return jr;
    }


    void service(JsonResult<T> jsonResult) throws Exception;

}
