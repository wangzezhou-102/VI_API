package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.service.SSOService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录接口
 * @author wangzezhou
 * @date 2019-07-19
 */
@Slf4j
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class SSOController {
    @Value("${tip.url}")
    private String tipurl;
    @Value("${spzn.host}")
    private String spznHost;
    @Value("${tip.host}")
    private String tipHost;
    @Resource
    private SSOService ssoService;

   @GetMapping("/getidtoken")// 1级请求路径 用于获取(可获取user_access_token) id_token
   public void getidToken(HttpServletRequest request){
       String id_token = request.getParameter("id_token");
       log.info("控制器中获取id_token:  {}",id_token);
   }

   @PostMapping("/spzn/logout")
    public JSONObject logout(HttpServletRequest request){
       DingdangUserRetriever.User resolveIdToken = (DingdangUserRetriever.User)request.getSession().getAttribute("resolveIdToken");
       JSONObject jsonObject = new JSONObject();
       jsonObject.put("sp_application_session_id",resolveIdToken.getExtendFields().get("sp_application_session_id"));
       jsonObject.put("appName",resolveIdToken.getApplicationName());
       jsonObject.put("purchaseId",resolveIdToken.getPurchaseId());
       jsonObject.put("logoutType","logout");
       return jsonObject;
   }

}