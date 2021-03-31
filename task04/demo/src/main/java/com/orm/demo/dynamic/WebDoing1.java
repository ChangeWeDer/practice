package com.orm.demo.dynamic;

import org.slf4j.Logger;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by oyhk on 16/3/20.
 * web 的 doing
 */
public interface WebDoing1<T> extends BaseDoing<T> {

    /**
     * 包装web 请求
     *
     * @param req HttpServletRequest
     * @param mod   Model
     * @param log     Logger
     * @return
     */
    default String go(HttpServletRequest req, Model mod, Logger log) {
        return this.go(req, mod, null, log);
    }

    /**
     * 包装web 请求
     *
     * @param r                  HttpServletRequest
     * @param m                  Model
     * @param redirectAttributes RedirectAttributes
     * @param log                Log
     * @return
     */
    default String go(HttpServletRequest r, Model m, RedirectAttributes redirectAttributes, Logger log) {
        ViewData viewData = new ViewData();
        try {
            // 显示 传入的参数
            this.showParams(r, log);
            this.service(r, m, viewData);
        } catch (Exception e) {
            try{
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }catch (NoTransactionException e1){}
            this.errorLog(log, e, viewData);
        }
        return viewData.view;
    }


    void service(HttpServletRequest req, Model mod, ViewData viewData) throws Exception;

}
