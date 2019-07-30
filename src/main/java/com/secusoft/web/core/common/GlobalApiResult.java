package com.secusoft.web.core.common;

import java.io.Serializable;

/**
 * 接口返回对象封装
 * @param <T>
 */
public class GlobalApiResult<T>  implements Serializable{

    private static final long serialVersionUID = -7221098877692032085L;

    private boolean success = true;

    private int code = 200;

    private String message;

    private T result;

    private GlobalApiResult() {
    }

    public static <T> GlobalApiResult<T> success() {
        GlobalApiResult<T> item = new GlobalApiResult<T>();
        item.result = null;
        item.message = "success";
        return item;
    }

    public static <T> GlobalApiResult<T> success(T result) {
        GlobalApiResult<T> item = new GlobalApiResult<T>();
        item.result = result;
        item.message = "success";
        return item;
    }

    public static <T> GlobalApiResult<T> failure(int errorCode, String errorMessage) {
        GlobalApiResult<T> item = new GlobalApiResult<T>();
        item.success = false;
        item.code = errorCode;
        item.message = errorMessage;
        return item;
    }

    public static <T> GlobalApiResult<T> failure(int errorCode) {
        GlobalApiResult<T> item = new GlobalApiResult<T>();
        item.success = false;
        item.code = errorCode;
        item.message = "failure";
        return item;
    }

    public static <T> GlobalApiResult<T> failure(String errorMessage) {
        GlobalApiResult<T> item = new GlobalApiResult<T>();
        item.success = false;
        item.code = 500;
        item.message = errorMessage;
        return item;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public T getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "GlobalApiResult{" +
                "success=" + success +
                ", code=" + code +
                ", message='" + message + '\'' +
                ", result=" + result +
                '}';
    }
}
