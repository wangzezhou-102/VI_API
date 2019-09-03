package com.secusoft.web.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.exception.BizExceptionEnum;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Value("${tip.host}")
    private String tipHost;

    //获取权限系统列表
    public ResultVo getPermissionList(){
        return null;
    }
    //获取用户详细信息
    public ResultVo getUserDetailInfo(DingdangUserRetriever.User resolveIdToken){
        log.info("获取用户信息请求开始...");
        HttpGet get = null;
        //处理请求路径
        try {
            //HttpClient有很多，可以根据个人喜好选用
            CloseableHttpClient httpClient = createSSLClientDefault();
            String geturl = "https://" + tipHost +"/api/bff/v1/user/detail/" + resolveIdToken.getUdAccountUuid() +
                    "?access_token=" + resolveIdToken.getAzp();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            get = new HttpGet(geturl);
            log.info("请求用户信息的完整路径： {}" , geturl);
            // 发送http请求
            HttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity1 = response.getEntity();
            String resultStr = EntityUtils.toString(entity1);
            if(StringUtils.isEmpty(resultStr)){
                return ResultVo.failure(BizExceptionEnum.SERVER_ERROR);
            }
            JSONObject resultJson = JSONObject.parseObject(resultStr);
            if((boolean)resultJson.get("success")){
                //返回结果
                JSONObject data = resultJson.getJSONObject("data");
                ResultVo result = new ResultVo();
                result.setData(data.getJSONObject("basicInformation"));
                log.info("获取用户信息成功！");
                return result;
            }else{
                return ResultVo.failure(BizExceptionEnum.ACCOUNT_ERROR);
            }
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
        return ResultVo.failure(BizExceptionEnum.SERVER_ERROR);
    }

    //获取指定用户的角色信息
    public ResultVo getUserRoleInfo(){
        return null;
    }
    //获取所有的角色信息
    public ResultVo getRoleList(){
        return null;
    }
    //获取角色已经关联的账户列表
    public ResultVo getRoleMemberList(){
        return null;
    }
    //获取根组织机构
    public ResultVo getRootOrg(){
        return null;
    }
    //获取指定组织机构下的组织结构列表
    public ResultVo getOrgList(){
        return null;
    }
    //获取指定组织结构详情
    public ResultVo getOrgDetailInfo(){
        return null;
    }
    //获取指定组织机构下的用户列表
    public ResultVo getOrgUserList(){
        return null;
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
