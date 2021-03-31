package com.orm.demo.dynamic;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oyhk on 16/3/20.
 *
 */
public class ViewData {

    public String view;
    public final String flashData = "flashData";
    public final String errorMsg = "errorMsg";
    // 提示
    public Map<String, Object> remind = new HashMap<>();

    public void redirect(String url) {
        this.view = "redirect:" + url;
    }

    /**
     * 跳转页面时 显示的参数
     * @param field
     * @param value
     */
    public void remindParam(String field, Object value) {
        remind.put(field, value);
    }

    /**
     * 当表单提交时,发生错误 跳转页面 , 使用SpringMvc RedirectAttributes.addFlashAttribute() 输出提示信息
     *
     * 请看 WebDoing.java 闭包封装了
     *
     * @param desc
     * @param redirectUrl
     */
    public void remind(String desc, String redirectUrl) {
        remind.put("desc", desc);
        this.redirect(redirectUrl);
    }

}
