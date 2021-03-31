package com.orm.demo.dynamic;

import org.slf4j.Logger;
import org.springframework.transaction.NoTransactionException;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by oyhk on 16/3/20.
 * web 的 doing
 */
public interface WebDoing<T> extends BaseDoing<T> {

    /**
     * 包装web 请求
     *
     * @param request HttpServletRequest
     * @param model   Model
     * @param log     Logger
     * @return
     */
    default ViewData go(HttpServletRequest request, Model model, Logger log) {
        return this.go(request, model, null, log);
    }

    /**
     * 包装web 请求
     *
     * @param request HttpServletRequest
     * @param model   Model
     * @param log     Logger
     * @return
     */
    default ViewData go(HttpServletRequest request, Model model, RedirectAttributes redirectAttributes, Logger log) {
        return this.go(null, null, null, request, model, redirectAttributes, log);
    }


    /**
     * 包装web 请求
     *
     * @param inputData          post 请求参数dto
     * @param redirectUrl        检验不通过,跳转的链接
     * @param validator          检验JSR-303
     * @param r                  HttpServletRequest
     * @param m                  Model
     * @param redirectAttributes RedirectAttributes
     * @param log                Log
     * @return
     */
    default ViewData go(Object inputData, String redirectUrl, Validator validator, HttpServletRequest r, Model m, RedirectAttributes redirectAttributes, Logger log) {
        ViewData viewData = new ViewData();
        try {
            // 显示 传入的参数
            this.showParams(r, log);
            // 验证 参数的合法性
            if (inputData != null && validator != null) {
                BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(inputData, viewData.flashData);
                validator.validate(inputData, bindingResult);
                if (bindingResult.hasErrors()) {
                    viewData.redirect(redirectUrl);
                    redirectAttributes.addFlashAttribute(viewData.errorMsg, bindingResult.getAllErrors().get(0).getDefaultMessage());
                    redirectAttributes.addFlashAttribute(viewData.flashData, inputData);
                    return viewData;
                }
            }
            this.service(r, m, viewData);
        } catch (Exception e) {
            try{
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            }catch (NoTransactionException e1){}
            this.errorLog(log, e, viewData);
        }
        return viewData;
    }


    void service(HttpServletRequest request, Model model, ViewData viewData) throws Exception;

}
