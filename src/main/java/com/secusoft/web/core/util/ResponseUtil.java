package com.secusoft.web.core.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * 返回结果处理类
 * @author ChenDong
 * @company 视在数科
 * @date 2019年6月6日
 */
public class ResponseUtil {

    /**
     * controller返回处理，后期扩展国际化
     * @author ChenDong
     * @date 2019年6月6日
     * @param status
     * @param body
     * @return
     */
    public static <T> ResponseEntity<T> handle(HttpStatus status, T body) {
        if(status == HttpStatus.OK) {
            return new ResponseEntity<T>(body,status);
        } else {
            return new ResponseEntity<T>(status);
        }
    }
}
