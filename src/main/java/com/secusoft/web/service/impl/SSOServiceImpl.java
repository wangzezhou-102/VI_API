package com.secusoft.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.SSOService;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.hibernate.validator.internal.util.privilegedactions.GetMethod;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

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

    //获取IdToken
    public void getIdToken(HttpSession session) {
        System.out.println("开始获取idToken");
        String posturl = "http://tap.hzgaaqfwpt.hzs.zj:8081/enduser/sp/sso/policejwt18";
        String redirecturi = "https://spzn.hzgaaqfwpt.hzs.zj/getidtoken";
        HttpGet getidtoken = null;
        try {
            String encode = URLEncoder.encode(redirecturi, "UTF-8");
            //HttpClient有很多，可以根据个人喜好选用
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            URIBuilder uriBuilder = new URIBuilder(posturl);
            uriBuilder.addParameter("enterpriseId","police");
            uriBuilder.addParameter("redirect_uri", encode);
            URI uri = uriBuilder.build();
            getidtoken = new HttpGet(uri);
            getidtoken.setHeader("Content-type", "application/json; charset=utf-8");
            // 发送http请求
            HttpResponse response = httpClient.execute(getidtoken);
            int statusCode = response.getStatusLine().getStatusCode();
            System.out.println("向后置发送idToken返回body:" + JSON.toJSONString(response.getEntity().getContent()));
            System.out.println("  实体  "+ EntityUtils.toString(response.getEntity(),"utf-8"));
            System.out.println("通过TAP获取idToken时状态码:  " + statusCode);
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
    //解析idToken
    @Override
    public DingdangUserRetriever.User resolveIdToken(String idToken) throws JoseException, IOException {
        System.out.println("前置开始解析idToken...");
        String publicKey = "{\"kty\":\"RSA\",\"kid\":\"1346614912326510837\",\"alg\":\"RS256\",\"n\":\"hOdf08cku1cEddGWHjOxalfqqmrMJ5LotXT28r0pgsw82uZiSNhi4kr1qVB7z3vUeqh0TffekWxsxGc0VXGoYrPYRkkS08old8CNZQjl7AbnY179kwPilburFuMXioYO55UgvXm2mpCBL8RKGiDSORlVXruBYhxGxZ8yAaloIPVZMTIBjhKtq_fc9K1fygjR7Q3BJJkDcLU92P1Jb8_EbpvRhkHzjKi-FcXbflPWY8dMQpksInp9c-AUByVvYQD3me94yVpyOcwVNUhT5sDUOHhbWjs0gkllY86GRqIHMpNk8VDI7BiXTny-etm7AGyU0_AJlwn4JcsERCqozH7n6w\",\"e\":\"AQAB\"}";
        //解析id_token
        DingdangUserRetriever retriever = new DingdangUserRetriever(idToken, publicKey);
        DingdangUserRetriever.User token = retriever.retrieve();
        System.out.println("前置解析成功!");
        return token;
    }
    //向后置发送IdToken
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
            HttpResponse response = httpClient.execute(get);
            System.out.println("向后置发送idToken返回body:" + JSON.toJSONString(response.getEntity().getContent()));
            System.out.println("  实体  "+ EntityUtils.toString(response.getEntity(),"utf-8"));
            System.out.println("向后置发送idToken返回状态码:" + response.getStatusLine().getStatusCode());
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


}
