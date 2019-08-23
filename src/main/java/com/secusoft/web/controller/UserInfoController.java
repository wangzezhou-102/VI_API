package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;

/**
 * 用户相关信息
 *
 * @author wangzezhou
 * @date 2019-08-22
 */
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    @PostMapping("/spzn/getuserdetailinfo")
    public JSONObject getUserDetailInfo(HttpServletRequest request) {
        HttpSession session = request.getSession();
        DingdangUserRetriever.User resolveIdToken = (DingdangUserRetriever.User) session.getAttribute("resolveIdToken");
        ResultVo userDetailInfo = userInfoService.getUserDetailInfo(resolveIdToken);
        return null;
    }
}