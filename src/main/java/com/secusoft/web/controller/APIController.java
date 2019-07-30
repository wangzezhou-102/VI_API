package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.core.common.Constants;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 业务API 对接 接口
 * @author wangzezhou
 * @date 2019-07-25
 */
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class APIController {
    @Resource
    private APIService apiService;

    //获取tip访问令牌
    @PostMapping("/spzn/*")
    public ResponseEntity requestAPI(@RequestBody JSONObject jsonObject, HttpServletRequest request){
        ResultVo resultVo = apiService.requestAPI(jsonObject, request);
        return ResponseUtil.handle(Constants.OK, resultVo);
    }



}

