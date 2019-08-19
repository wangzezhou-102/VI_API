package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.common.Constants;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.SSOService;
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
       System.out.println("控制器中获取id_token:  " + id_token);
   }

    public void getUserInfo(String uuid,String access_token) {
        //向(4A)139发送请求获取用户详细信息
        HttpGet getidtoken = null;
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = HttpClients.createDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            getidtoken = new HttpGet("http://139.64.48.202:8081/api/bff/v1/user/detail/"+uuid+"?access_token="+access_token);
            // 构造消息头
            getidtoken.setHeader("Content-type", "application/json; charset=utf-8");
            // 发送http请求
            HttpResponse response = httpClient.execute(getidtoken);
            //System.out.println("通过tap获取idtoken时响应信息:"+response);
            HttpEntity entity = response.getEntity();
            String userInfo = EntityUtils.toString(entity, "UTF-8");
            System.out.println("前置访问返回整体Response数据:"+ response);
            System.out.println("前置在4A访问的用户详细信息:" + userInfo);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (getidtoken != null) {
                try {//断开链接
                    getidtoken.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}