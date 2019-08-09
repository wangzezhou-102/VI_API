package com.secusoft.web.controller;

import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.UserInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
public class UserInfoController {
    @Resource
    private UserInfoService userInfoService;

    //获取用户信息
    @GetMapping("/getuserdetailinfo")
    public void getUserDetailInfo(HttpServletRequest request){
        HttpSession session = request.getSession();
        DingdangUserRetriever.User resolveIdToken = (DingdangUserRetriever.User)session.getAttribute("resolveIdToken");
        ResultVo userDetailInfo = userInfoService.getUserDetailInfo(resolveIdToken);
    }

}
