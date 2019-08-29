package com.secusoft.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.core.common.Constants;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import org.apache.http.HttpEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 业务API 对接 接口
 * @author wangzezhou
 * @date 2019-07-25
 */
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class APIController {
    @Autowired
    private APIService apiService;
    //获取tip访问令牌
    @RequestMapping("/spzn/*")
    public ResponseEntity requestAPI(@RequestBody(required = false) Object param, HttpServletRequest request){
        ResultVo resultVo = apiService.requestAPI(param, request);
        return ResponseUtil.handle(Constants.OK, resultVo);
    }
    //通过TIP 访问后置图像
    @GetMapping("/spzn/pic")
    public void req(HttpServletRequest request, HttpServletResponse response){
        apiService.requestImage(request,response);
    }
    
    //通过TIP 访问后置文件
    @GetMapping("/spzn/file")
    public void reqfile(HttpServletRequest request, HttpServletResponse response){
        apiService.requestFile(request,response);
    }

}
