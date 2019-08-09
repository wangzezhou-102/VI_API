/*
package com.secusoft.web.task;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.impl.APIServiceImpl;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TipTokenTask {
    private static final Logger log = LoggerFactory.getLogger(TipTokenTask.class);
    @Resource
    private APIService apiService;
    @Value("${spzn.host}")
    private String spznHost;
    @Value("${spzn.appid}")
    private String appId;
    @Value("${spzn.appkey}")
    private String appKey;

    private FingerTookit fingerTookit;
    @Scheduled(cron="* * 0/11 * * ?")
    public void syncGetTipToken() {
        log.info("获取tip访问令牌 start");
        String userAccessToken = (String) session.getAttribute("userAccessToken");
        //检查参数
        if (StringUtils.isEmpty(userAccessToken)) {
            throw new InvalidParameterException("userAccessToken empty");
        }
    //填充消息
    JSONObject jobj = new JSONObject();
    //jobj.put("app_id", appId);
        jobj.put("primary_token", userAccessToken);
        */
/*//*
/challenge和mid可以不传(建议传，提高安全性)
        if(!StringUtils.isEmpty(challenge) && !StringUtils.isEmpty(mid)) {
            jobj.put("challenge", challenge);
            jobj.put("mid", mid);
        }*//*

    //生成指纹
    fingerTookit = new FingerTookit(appId, appKey);
    String fingerprint = fingerTookit.buildFingerprint(jobj);
        jobj.put("fingerprint", fingerprint);
        System.out.println("获取tip传参:" + jobj.toString());
    //发送请求
    HttpPost post = null;
        try {
        //https不验证证书
        HttpClient httpClient = createSSLClientDefault();
        post = new HttpPost("https://" + tipUrl + "/sts/token");
        // 构造消息头
        post.setHeader("Content-type", "application/json; charset=utf-8");
        // 构建消息实体
        StringEntity entity = new StringEntity(jobj.toJSONString(), Charset.forName("UTF-8"));
        entity.setContentEncoding("UTF-8");
        // 发送Json格式的数据请求
        entity.setContentType("application/json");
        post.setEntity(entity);
        HttpResponse response = httpClient.execute(post);
        System.out.println("获取tip请求响应：" + response);
        // 检验http返回码
        int statusCode = response.getStatusLine().getStatusCode();
        //TIP返回201 (400 500 表示失败)
        if (statusCode == HttpStatus.SC_CREATED) {
            String result = null;
            result = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject responseObj = JSONObject.parseObject(result);
            //校验指纹
            boolean b = fingerTookit.checkFingerprint(responseObj);
            //获取tip_access_token
            String access_token = (String) responseObj.get("access_token");
            Integer expiresIn = (Integer) responseObj.get("expires_in");
            //String uid = (String) responseObj.get("uid");
            //保存过期时间
            session.setAttribute("expiresIn", expiresIn);
            //session.setAttribute("uid", uid);
            //保存tip_access_token
            session.setAttribute("tipAccessToken", access_token);
            System.out.println("TIP访问令牌：" + access_token);
            System.out.println("TIP过期时间:"+  expiresIn);
        }
        System.out.println("tip令牌访问获取失败状态: " + statusCode);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        if (post != null) {
            try {
                post.releaseConnection();
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
*/
