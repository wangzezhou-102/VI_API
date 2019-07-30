package com.secusoft.web.core.exception;

import com.secusoft.web.core.common.GlobalApiResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.UndeclaredThrowableException;
import static com.secusoft.web.core.support.HttpKit.getRequest;

/**
 * 全局的的异常拦截器（拦截所有的控制器）（带有@RequestMapping注解的方法上都会拦截）
 *
 *
 * @date 2016年11月12日 下午3:19:56
 */
@ControllerAdvice(annotations = {RestController.class, Controller.class})
public class GlobalExceptionHandler {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * 拦截业务异常
     *
     *
     */
    @ExceptionHandler(BussinessException.class)
    @ResponseBody
    public GlobalApiResult<Object> bussinessException(BussinessException e) {
        getRequest().setAttribute("tip", e.getMessage());
        log.error("业务异常:", e);
        return GlobalApiResult.failure(e.getCode(), e.getMessage());
    }

    /**
     * 无权访问该资源
     *
     *
     */
    @ExceptionHandler(UndeclaredThrowableException.class)
    @ResponseBody
    public GlobalApiResult<Object> credentials(UndeclaredThrowableException e) {
        getRequest().setAttribute("tip", "权限不足");
        log.error("权限异常!", e);
        return GlobalApiResult.failure(BizExceptionEnum.NO_PERMITION.getCode(),BizExceptionEnum.NO_PERMITION.getMessage());
    }

    /**
     * 拦截未知的运行时异常
     *
     *
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public GlobalApiResult<Object> runtimeException(RuntimeException e) {
        getRequest().setAttribute("tip", "服务器未知运行时异常");
        log.error("运行时异常:", e);
        return GlobalApiResult.failure(BizExceptionEnum.SERVER_ERROR.getCode(),BizExceptionEnum.SERVER_ERROR.getMessage());
    }


//    private void assertAjax(HttpServletRequest request, HttpServletResponse response) {
//        if (request.getHeader("x-requested-with") != null
//                && request.getHeader("x-requested-with").equalsIgnoreCase("XMLHttpRequest")) {
//            //如果是ajax请求响应头会有，x-requested-with
//            response.setHeader("sessionstatus", "timeout");//在响应头设置session状态
//        }
//    }

}
