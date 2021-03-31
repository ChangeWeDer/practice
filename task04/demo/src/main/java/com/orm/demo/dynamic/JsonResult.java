package com.orm.demo.dynamic;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Created by KingSum on 14-6-28.
 * update by zhangjh on 2017-08-26
 * static final数组变为private
 */
public class JsonResult<T> {

    public final static String CD_0 = "0";
    public final static String CD_1 = "1";
    public final static String CD_2 = "2";
    public final static String CD_3 = "3";
    public final static String CD_101 = "101";
    public final static String CD_102 = "102";
    public final static String CD_103 = "103";
    public final static String CD_10101 = "10101";
    public final static String CD_10102 = "10102";
    public final static String CD_10103 = "10103";
    public final static String CD_10104 = "10104";
    public final static String CD_10105 = "10105";
    public final static String CD_10106 = "10106";

    private static final String CD3[] = {"3", "温馨提示:"};
    private static final String CD2[] = {"404", "参数错误:"};
    private static final String CD1[] = {"200", "成功"};
    private static final String CD0[] = {"500", "失败"};

    private static final String CD101[] = {"101", "用户名不存在"};
    private static final String CD102[] = {"102", "用户名状态异常"};
    private static final String CD103[] = {"103", "密码错误"};

    private static final String CD10101[] = {"10101", "没权限"};
    private static final String CD10102[] = {"10102", "没商家权限"};
    private static final String CD10103[] = {"10103", "没平台权限"};
    private static final String CD10104[] = {"10104", "domain不存在"};
    private static final String CD10105[] = {"10105", "没平台权限"};
    private static final String CD10106[] = {"10106", "没启用"};

    public JsonResult() {
    }


    public JsonResult(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }


    public JsonResult(String code) {
        saveResult(code);
    }

    public JsonResult(String code, String desc, T data) {
        this.code = code;
        this.desc = desc;
        this.data = data;
    }


    public void remind(String desc, Logger log) {
        this.code = JsonResult.CD3[0];
        this.desc = JsonResult.CD3[1] + desc;
        log.error("remind , desc : {}", desc);
    }

    private void saveJsonResult(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void errorParam(String desc, Logger log) {
        this.code = JsonResult.CD2[0];
        this.desc = JsonResult.CD2[1] + desc;
//        if (log != null) {
        log.error("errorParam , desc : {}", desc);
//        }
    }

    public void custom(String code, String desc, Logger log) {
        this.code = code;
        this.desc = desc;
        log.error("custom , code : {} , desc : {}", code, desc);
    }

    public void saveResult(String codeInput) {
        saveResult(codeInput, null);
    }

    public void saveResult(String codeInput, Logger log) {
        saveResult(codeInput, null, null);
    }

    public void saveResult(String codeInput, String desc, Logger log) {
        switch (codeInput) {
            case "CD0":
                saveJsonResult(CD0[0], CD0[1]);
                break;
            case "CD1":
                saveJsonResult(CD1[0], CD1[1]);
                break;
            case "CD2":
                saveJsonResult(CD2[0], CD2[1]);
                break;
            case "CD3":
                saveJsonResult(CD3[0], CD3[1]);
                break;
            case "CD101":
                saveJsonResult(CD101[0], CD101[1]);
                break;
            case "CD102":
                saveJsonResult(CD102[0], CD102[1]);
                break;
            case "CD103":
                saveJsonResult(CD103[0], CD103[1]);
                break;
            case "CD10101":
                saveJsonResult(CD10101[0], CD10101[1]);
                break;
            case "CD10102":
                saveJsonResult(CD10102[0], CD10102[1]);
                break;
            case "CD10103":
                saveJsonResult(CD10103[0], CD10103[1]);
                break;
            case "CD10104":
                saveJsonResult(CD10104[0], CD10104[1]);
                break;
            case "CD10105":
                saveJsonResult(CD10105[0], CD10105[1]);
                break;
            case "CD10106":
                saveJsonResult(CD10106[0], CD10106[1]);
                break;
            default:
                saveJsonResult(CD1[0], CD1[1]);
                break;

        }

        if (StringUtils.isNotBlank(desc)) {
            setDesc(desc);
        }
        if (null != log) {
            log.error(" code : {} , desc : {}", getCode(), getDesc());
        }


    }

    public String code = JsonResult.CD1[0];
    public String desc = JsonResult.CD1[1];
    public T data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JsonResult{" +
                "code='" + code + '\'' +
                ", desc='" + desc + '\'' +
                ", data=" + data +
                '}';
    }
}
