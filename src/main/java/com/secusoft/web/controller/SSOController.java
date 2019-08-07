package com.secusoft.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.common.Constants;
import com.secusoft.web.core.util.ResponseUtil;
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
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URI;
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
    @Resource
    private SSOService ssoService;
    @Value("${tip.url}")
    private String tipurl;
    @Value("${spzn.host}")
    private String spznHost;

    //获取用户详细信息
   /* @PostMapping("/getuserdetailinfo")
    public ResponseEntity getUserDetailInfo(HttpSession session){
        ResultVo userDetailInfo = null;
        try{
            userDetailInfo = ssoService.getUserDetailInfo(session);
        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseUtil.handle(Constants.OK, userDetailInfo);
    }*/
    //获取id_token
    @GetMapping("/spzn/getidtoken")
    public ResponseEntity getIdToken(@RequestParam("id_token")String idToken,HttpSession session){
        System.out.println("从4A获取到的数据所有值为(从中获取id_token): "+idToken);
        session.setAttribute("idToken",idToken);
        try {
            DingdangUserRetriever.User token = resolveIdToken(session);
            //保存解析的idToken
            session.setAttribute("resolveIdToken",token);
            String uuid = token.getUdAccountUuid();
            String access_token = token.getAzp();
            System.out.println("前置解析获得idToken的uuid:" + token.getUdAccountUuid());
            System.out.println("前置解析获得idToken的access_token:" + token.getAzp());
            System.out.println("前置解析或得IDToken的用户名:"+token.getUsername());
            System.out.println("IdToken的过期时间:"+token.getExp());
            /*System.out.println("前置请求4A获取用户信息中...");
            getUserInfo(uuid,access_token);
            System.out.println("前置用户信息获取完成!");*/
            sendIdToken(session);
            System.out.println("向后置发送idToken信息完成!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseUtil.handle(Constants.OK, null);
    }

    //获取IdToken
    public void sendIdToken(HttpSession session) {
        System.out.println("idToken往后置发送开始：");
        String tipAccessToken = (String) session.getAttribute("tipAccessToken");
        String userAccessToken = (String) session.getAttribute("userAccessToken");
        HttpGet get = null;
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = createSSLClientDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            String idToken = (String)session.getAttribute("idToken");
            URIBuilder uriBuilder = new URIBuilder("https://" + tipurl + "/spzn/receiveidtoken");
            uriBuilder.addParameter("idToken",idToken);
            URI uri = uriBuilder.build();
            get = new HttpGet(uri);
            System.out.println("向后置传递的idToken:" + idToken);
            // 构造消息头
            get.setHeader("Content-type", "application/json; charset=utf-8");
            // 填入双令牌
            get.setHeader("X-trustuser-access-token", userAccessToken);
            get.setHeader("X-trustagw-access-token", tipAccessToken);
            get.setHeader("Host", spznHost);
            // 发送http请求
            HttpResponse response = httpClient.execute(get);
//			System.out.println("业务api对接返回数据：" + response + " 返回实体：" + resultStr);
            System.out.println("后置发送返回状态信息:"+response);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (get != null) {
                try {//断开链接
                    get.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static CloseableHttpClient createSSLClientDefault() {
        try {
            //使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                // 信任所有
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
            //NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
            //有效的SSL会话并匹配到目标主机。
            HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return HttpClients.createDefault();
    }
    //解析idToken
    public DingdangUserRetriever.User resolveIdToken(HttpSession session) throws JoseException, IOException {
        String publicKey = "{\"kty\":\"RSA\",\"kid\":\"1346614912326510837\",\"alg\":\"RS256\",\"n\":\"hOdf08cku1cEddGWHjOxalfqqmrMJ5LotXT28r0pgsw82uZiSNhi4kr1qVB7z3vUeqh0TffekWxsxGc0VXGoYrPYRkkS08old8CNZQjl7AbnY179kwPilburFuMXioYO55UgvXm2mpCBL8RKGiDSORlVXruBYhxGxZ8yAaloIPVZMTIBjhKtq_fc9K1fygjR7Q3BJJkDcLU92P1Jb8_EbpvRhkHzjKi-FcXbflPWY8dMQpksInp9c-AUByVvYQD3me94yVpyOcwVNUhT5sDUOHhbWjs0gkllY86GRqIHMpNk8VDI7BiXTny-etm7AGyU0_AJlwn4JcsERCqozH7n6w\",\"e\":\"AQAB\"}";
        String idToken = (String)session.getAttribute("idToken");
        //解析id_token
        System.out.println("开始解析idToken");
        DingdangUserRetriever retriever = new DingdangUserRetriever(idToken, publicKey);
        DingdangUserRetriever.User token = retriever.retrieve();
        System.out.println("解析成功!");
        return token;
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