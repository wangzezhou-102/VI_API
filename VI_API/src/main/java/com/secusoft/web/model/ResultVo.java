package com.secusoft.web.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.secusoft.web.core.exception.BizExceptionEnum;

/**
 * 返回VO
 * @author ChenDong
 * @company 视在数科
 * @date 2019年5月27日
 */
public class ResultVo {

    /**
     * 业务错误码
     */
    private int code = BizExceptionEnum.OK.getCode();

    /**
     * 错误信息
     */
    private String message = BizExceptionEnum.OK.getMessage();

    /**
     * 返回信息
     */
    @JsonInclude(Include.NON_EMPTY)
    private Object data;

    /**
     * 总数
     */
    @JsonInclude(Include.NON_EMPTY)
    private Long total;

    public ResultVo() {
    }

    public static ResultVo success() {
        ResultVo resultVo = new ResultVo();
        resultVo.data = null;
        resultVo.message = "success";
        return resultVo;
    }

    public static ResultVo success(Object result) {
        ResultVo resultVo = new ResultVo();
        resultVo.data = result;
        resultVo.message = "success";
        return resultVo;
    }
    public static ResultVo success(Object result, Long totalCount) {
        ResultVo resultVo = new ResultVo();
        resultVo.data = result;
        resultVo.message = "success";
        resultVo.total= totalCount;
        return resultVo;
    }

    public static ResultVo failure(BizExceptionEnum error) {
        return failure(error.getCode(), error.getMessage());
    }
    
    public static ResultVo failure(int errorCode, String errorMessage) {
        ResultVo resultVo = new ResultVo();
        resultVo.code = errorCode;
        resultVo.message = errorMessage;
        return resultVo;
    }

    public static ResultVo failure(int errorCode) {
        ResultVo resultVo = new ResultVo();
        resultVo.code = errorCode;
        return resultVo;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "ResultVo [code=" + code + ", message=" + message + ", data=" + data + ", total=" + total + "]";
    }

}
