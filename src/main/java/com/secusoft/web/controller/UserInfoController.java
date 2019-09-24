package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.exception.BizExceptionEnum;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * 用户相关信息
 *
 * @author wangzezhou
 * @date 2019-08-22
 */
@Slf4j
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    @PostMapping("/spzn/getuserdetailinfo")
    public ResponseEntity getUserDetailInfo(HttpServletRequest request) {
        HttpSession session = request.getSession();
        DingdangUserRetriever.User resolveIdToken = (DingdangUserRetriever.User) session.getAttribute("resolveIdToken");
        ResultVo userDetailInfo = userInfoService.getUserDetailInfo(resolveIdToken);
        return ResponseUtil.handle(HttpStatus.OK, userDetailInfo);
    }

    @PostMapping("/spzn/getpsmenu")
    public ResponseEntity getMenuPermissions (HttpServletRequest request) {
        log.info("请求一级菜单...");
        ResultVo menuPermissions = userInfoService.getMenuPermissions(request);
        return ResponseUtil.handle(HttpStatus.OK, menuPermissions);
    }

    @PostMapping("/spzn/getpsaccesstoken")
    public ResponseEntity getRolePermissions1 () {
        log.info("获取角色权限信息");
        JSONObject psAccessToken = userInfoService.getPsAccessToken();
        return ResponseUtil.handle(HttpStatus.OK, psAccessToken);
    }

    @PostMapping("/spzn/getpsroleps")
    public ResponseEntity getRolePermissions2 (HttpServletRequest request) {
        ResultVo rolePermissions = userInfoService.getRolePermissions(request);
        return ResponseUtil.handle(HttpStatus.OK, rolePermissions);
    }



}