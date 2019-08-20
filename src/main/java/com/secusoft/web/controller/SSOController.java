package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.common.Constants;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.SSOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

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
    @Resource
    private SSOService ssoService;

   @GetMapping("/getidtoken")// 1级请求路径 用于获取(可获取user_access_token) id_token
   public void getidToken(HttpServletRequest request){
       String id_token = request.getParameter("id_token");
       log.info("控制器中获取id_token:  " + id_token);
   }

}