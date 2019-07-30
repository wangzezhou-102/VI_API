package com.secusoft.web.core.common;

import org.springframework.http.HttpStatus;

/**
 * 常量类
 * @author ChenDong
 * @company 视在数科
 * @date 2019年6月6日
 */
public class Constants {

    // 返回成功状态码
    public static final HttpStatus OK = HttpStatus.OK;
    
    // 天擎返回成功信息
    public static final String SUCCESS = "SUCCESS";
    
    // 天擎返回失败信息
    public static final String FAILED = "FAILED";
    
    // 设备流状态信息 已启动
    public static final int STREAM_STATE_VALID = 1;
    
    // 设备流状态信息 未启动
    public static final int STREAM_STATE_NOT_VALID = 0;
}
