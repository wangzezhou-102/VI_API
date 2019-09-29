package com.secusoft.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.exception.BizExceptionEnum;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.MenuBean;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.UserInfoService;
import com.secusoft.web.utils.APIHttpClientPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Value("${tip.host}")
    private String tipHost;
    @Value("${spzn.psid}")
    private String psid;
    @Value("${spzn.psappkey}")
    private String psappkey;
    @Value("${spzn.psappsecret}")
    private String psappsecret;

    //获取权限系统列表
    public ResultVo getPermissionList(){
        return null;
    }
    //获取用户详细信息
    public ResultVo getUserDetailInfo(DingdangUserRetriever.User resolveIdToken){
        log.info("获取用户详细信息请求开始...");
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
                //log.info("{}",data.getJSONObject("basicInformation"));
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
    //获取权限系统令牌 access_token
    public JSONObject getPsAccessToken() {
        log.info("获取权限系统令牌");
        String url = "https://" + tipHost + "/oauth/token?client_id="+psappkey+"&client_secret="+psappsecret+"&grant_type=client_credentials&scop=read";
        HttpPost post = null;
        try {
            //https不验证证书
            HttpClient httpClient = createSSLClientDefault();
            post = new HttpPost(url);
            log.info("url : {}", url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 构建消息实体
            //StringEntity entity = new StringEntity(JSON.toJSONString(params), Charset.forName("UTF-8"));
            // 发送Json格式的数据请求
            //entity.setContentType("application/form");
            //post.setEntity(entity);
            HttpResponse response = httpClient.execute(post);
            // 检验http返回码
            int statusCode = response.getStatusLine().getStatusCode();
            //TIP返回201 (400 500 表示失败)
            if (statusCode == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject responseObj = JSONObject.parseObject(result);
                if (responseObj.getString("access_token") != null) {
                    log.info("权限系统信息： {}", responseObj);
                    return responseObj;
                }
            }
            log.info("权限系统令牌获取失败状态: {}", statusCode);
            //log.info("权限系统获取角色和权限失败信息: {}", response);
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
        return null;
    }
    //获取指定用户的角色信息
    public ResultVo getUserRoleInfo(){ return null;}
    //获取用户的角色和权限信息
    public ResultVo getRolePermissions (HttpServletRequest request) {
        JSONObject psAccessToken = getPsAccessToken();
        if (psAccessToken == null) {
            log.info("重新获取ps_access_token");
            psAccessToken = getPsAccessToken();
        }
        String access_token = psAccessToken.getString("access_token");
        String url = "https://" + tipHost + "/api/bff/v1.2/developer/ps/user_role_permissions?access_token="+access_token;
        HttpSession session = request.getSession();
        DingdangUserRetriever.User resolveIdToken = (DingdangUserRetriever.User)session.getAttribute("resolveIdToken");
        String sub = resolveIdToken.getSub();
        HashMap<String, String> params = new HashMap<>();
        //params.put("access_token",access_token);
        //log.info("ps_access_token : {}", access_token);
        params.put("psId", psid);
        params.put("username", sub);
        HttpPost post = null;
        try {
            //https不验证证书
            HttpClient httpClient = createSSLClientDefault();
            post = new HttpPost(url);
            // 构造消息头
            post.setHeader("Content-type", "application/json; charset=utf-8");
            // 构建消息实体
            StringEntity paramsEntity = new StringEntity(JSON.toJSONString(params), "UTF-8");
            // 发送Json格式的数据请求
            paramsEntity.setContentType("application/json");
            post.setEntity(paramsEntity);
            log.info("params: {}",JSON.toJSONString(params));
            HttpResponse response = httpClient.execute(post);
            // 检验http返回码
            int statusCode = response.getStatusLine().getStatusCode();
            //返回200 (400 500 表示失败)
            if (statusCode == HttpStatus.SC_OK) {
                String result = EntityUtils.toString(response.getEntity(), "UTF-8");
                JSONObject responseObj = JSONObject.parseObject(result);
                JSONObject data = responseObj.getJSONObject("data");
                //log.info("返回 data 数据: {} ", data);
                return ResultVo.success(data);
            }
            log.info("权限系统获取角色和权限失败状态: {}", statusCode);
            log.info("权限系统获取角色和权限失败信息: {}", response);
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
        return ResultVo.failure(BizExceptionEnum.SERVER_ERROR);
    }
    //获取所有的角色信息
    public ResultVo getRoleList(){
        String url = "https://" + tipHost + "/api/bff/v1.2/developer/ps/all_roles";
        HashMap<String, String>  params = new HashMap<>();
        JSONObject psAccessToken = getPsAccessToken();
        //判断是否获取到access_token
        if (psAccessToken == null) {
            //重新获取ps_access_token
            psAccessToken = getPsAccessToken();
        }
        String access_token = psAccessToken.getString("access_token");
        params.put("access_token", access_token);
        params.put("psid", psid);
        StringEntity stringEntity = new StringEntity(JSONObject.toJSONString(params), "UTF-8");
        String roles = APIHttpClientPool.fetchByPostMethod("https",url, stringEntity);
        JSONObject jsonObject = JSONObject.parseObject(roles);
        if ("200".equals(jsonObject.getString("code"))) {
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray list = data.getJSONArray("list");
            return ResultVo.success(list);
        }
        return ResultVo.failure(BizExceptionEnum.SERVER_ERROR);
    }
    //获取角色已经关联的账户列表
    public ResultVo getRoleMemberList(){ return null; }
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
    public ResultVo getOrgUserList(){ return null; }
    //获取功能菜单
    public ResultVo getMenuPermissions(HttpServletRequest request){
        /*ResultVo rolePermissionsResultVo = getRolePermissions(request);
        if (rolePermissionsResultVo.getCode() != 1001010) {
            rolePermissionsResultVo = getRolePermissions(request);
        }
        //log.info("用户角色和权限信息:  {}",rolePermissionsResultVo.getData());
        JSONObject data = (JSONObject)rolePermissionsResultVo.getData();
        JSONObject Permissions = new JSONObject();
        JSONArray permissionsnew = new JSONArray();
        //获取角色权限列表
        JSONArray  rolePermissions = data.getJSONArray("rolePermissions");
        log.info("data: {}", rolePermissions);
        //添加用户名
        Permissions.put("username",data.getString("username"));
        //添加权限（去重）
        for (int i=0;i<rolePermissions.size();i++) {
            JSONObject jsonObject = rolePermissions.getJSONObject(i);
            JSONArray permissionsold = jsonObject.getJSONArray("permissions");
            for (int j=0;j<permissionsold.size();j++) {
                boolean isSame = false;
                JSONObject jsonObject1 = permissionsold.getJSONObject(j);
                //比较uuid是否相等（目前隐藏全城布控、离线分析、结伴同行）
                for (int k=0;k<permissionsnew.size();k++) {
                    if (permissionsnew.getJSONObject(k).getString("uuid").equals(jsonObject1.getString("uuid")) ) {
                        isSame = true;
                    }
                    if (jsonObject1.getString("name").equals("全城布控") || jsonObject1.getString("name").equals("离线分析")) {
                        isSame = true;
                    }
                }
                if (!isSame) {
                    permissionsnew.add(jsonObject1);
                }
            }
        }
        JSONArray jsonArray = new JSONArray();
        for (int i=1;i<=8;i++) {
            for (int j=0;j<permissionsnew.size();j++) {
                if (permissionsnew.getJSONObject(j).getInteger("displayOrder") == i) {
                    jsonArray.add(permissionsnew.getJSONObject(j));
                }
            }
        }
        Permissions.put("permissions", jsonArray);*/
        //log.info("Permssions {}",Permissions);
        //log.info("permssions {}",Permissions.getJSONArray("permissions"));
        /*JSONObject jsonObject1 = permissions.getJSONObject(i);
        MenuBean menuBean = new MenuBean();
        menuBean.setUuid(jsonObject1.getString("uuid"));
        menuBean.setName(jsonObject1.getString("name"));
        menuBean.setType(jsonObject1.getString("type"));
        menuBean.setPermissionValue(jsonObject1.getString("permissionValue"));
        menuBean.setDisplayOrder(jsonObject1.getInteger("displayOrder"));
        menuBean.setRelationUrl(jsonObject1.getString("relationUrl"));*/
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        MenuBean menuBean = new MenuBean();
        menuBean.setName("视频搜索");
        menuBean.setType("menu");
        menuBean.setPermissionValue("search");
        menuBean.setDisplayOrder(1);
        menuBean.setRelationUrl("/bkzx/search");
        MenuBean menuBean2 = new MenuBean();
        menuBean2.setName("全城布控");
        menuBean2.setType("menu");
        menuBean2.setPermissionValue("cityControl");
        menuBean2.setDisplayOrder(2);
        menuBean2.setRelationUrl("/bkzx/cityControl");
        MenuBean menuBean3 = new MenuBean();
        menuBean3.setName("区域布控");
        menuBean3.setType("menu");
        menuBean3.setPermissionValue("realtimeControl");
        menuBean3.setDisplayOrder(3);
        menuBean3.setRelationUrl("/bkzx/realtimeControl");
        MenuBean menuBean4 = new MenuBean();
        menuBean4.setName("视频追踪");
        menuBean4.setType("menu");
        menuBean4.setPermissionValue("realtimeTrack");
        menuBean4.setDisplayOrder(4);
        menuBean4.setRelationUrl("/bkzx/realtimeTrack");
        MenuBean menuBean5 = new MenuBean();
        menuBean5.setName("结伴同行");
        menuBean5.setType("menu");
        menuBean5.setPermissionValue("groupwalk");
        menuBean5.setDisplayOrder(5);
        menuBean5.setRelationUrl("/bkzx/groupwalk");
        MenuBean menuBean6 = new MenuBean();
        menuBean6.setName("走失寻人");
        menuBean6.setType("menu");
        menuBean6.setPermissionValue("videofind");
        menuBean6.setDisplayOrder(6);
        menuBean6.setRelationUrl("/bkzx/videofind");
        MenuBean menuBean7 = new MenuBean();
        menuBean7.setName("异常人流预警");
        menuBean7.setType("menu");
        menuBean7.setPermissionValue("abnormalflow");
        menuBean7.setDisplayOrder(7);
        menuBean7.setRelationUrl("/bkzx/abnormalflow");
        MenuBean menuBean8 = new MenuBean();
        menuBean8.setName("离线分析");
        menuBean8.setType("menu");
        menuBean8.setPermissionValue("offlineAnalise");
        menuBean8.setDisplayOrder(8);
        menuBean8.setRelationUrl("/bkzx/offlineAnalise");
        jsonArray.add(menuBean);
        //jsonArray.add(menuBean2);
        jsonArray.add(menuBean3);
        jsonArray.add(menuBean4);
        //jsonArray.add(menuBean5);
        jsonArray.add(menuBean6);
        jsonArray.add(menuBean7);
        //jsonArray.add(menuBean8);
        jsonObject.put("username", "bkzx");
        jsonObject.put("permissions", jsonArray);
        //以上为模拟数据
        JSONArray permissions = jsonObject.getJSONArray("permissions");
        //log.info("有序菜单： {}",jsonArray);
        //log.info("无序菜单：{}",permissionsnew);
        return ResultVo.success(permissions);
    }
    //判断用户是否有该角色
    public Boolean hasRole () { return false; }
    //https
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
