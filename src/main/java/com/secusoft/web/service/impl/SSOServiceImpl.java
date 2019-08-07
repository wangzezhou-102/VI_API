package com.secusoft.web.service.impl;

import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.service.SSOService;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Service
public class SSOServiceImpl implements SSOService {
    @Value("${spzn.appid}")
    private String appid;
    @Value("${spzn.appkey}")
    private String appkey;
    @Value("${tip.url}")
    private String tipurl;
    @Value("${spzn.host}")
    private String spznHost;
    private FingerTookit fingerTookit;

    //获取IdToken
    public void getIdToken(HttpSession session) {
        System.out.println("开始获取idToken");
        String posturl = "http://tap.hzgaaqfwpt.hzs.zj:8081/enduser/sp/sso/policejwt18";
        String redirecturi = "https://172.16.24.28:8105/spzn/getidtoken";
        HttpGet getidtoken = null;
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = HttpClients.createDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            getidtoken = new HttpGet(posturl+"?enterpriseId=police&redirect_uri="+redirecturi);
            // 构造消息头
            getidtoken.setHeader("Content-type", "application/json; charset=utf-8");
            // 发送http请求
            HttpResponse response = httpClient.execute(getidtoken);
            //System.out.println("通过tap获取idtoken时响应信息:"+response);
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
    //获取IdToken
    public void sendIdToken(HttpServletRequest request) {
        System.out.println("idToken往后置发送开始：");
        HttpSession session = request.getSession();
        String tipAccessToken = (String) session.getAttribute("tipAccessToken");
        String userAccessToken = (String) session.getAttribute("userAccessToken");
        HttpGet get = null;
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = createSSLClientDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            String idToken = (String)session.getAttribute("idToken");
            get = new HttpGet("https://" + tipurl + "/spzn/receiveidtoken?idToken="+ idToken);
            System.out.println("向后置传递的idToken:" + idToken);
            // 构造消息头
            get.setHeader("Content-type", "application/json; charset=utf-8");
            // 填入双令牌
            get.setHeader("X-trustuser-access-token", userAccessToken);
            get.setHeader("X-trustagw-access-token", tipAccessToken);
            get.setHeader("Host", spznHost);
            // 发送http请求
            System.out.println("X-trustuser-access-token   "+userAccessToken);
            System.out.println("X-trustagw-access-token    "+tipAccessToken);
            System.out.println("Host:                      "+spznHost);
            HttpResponse response = httpClient.execute(get);
//			System.out.println("业务api对接返回数据：" + response + " 返回实体：" + resultStr);
            System.out.println("发送返回状态信息:"+response);
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
   /* //判断用户登录
    public ResultVo SSO(JSONObject jsonObject){
        //TODO
        //user_access_token(格式不确定,封装成bean,在session中获取进行判断)
        return ResultVo.success();
    }
    //获取用户详细信息
    public ResultVo getUserDetailInfo(HttpSession session)throws JoseException, IOException{
        DingdangUserRetriever.User token = resolveIdToken(session);
        System.out.println("IdToken信息");
        System.out.println("token的uuid:"+token.getUdAccountUuid());
        System.out.println("token的access_token:"+token.getAzp());
        return null;
    }*/


}
