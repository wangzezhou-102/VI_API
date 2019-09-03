package com.secusoft.web.core.filter;

import com.alibaba.fastjson.JSON;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.SSOService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.HttpGet;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springfox.documentation.spring.web.json.Json;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

@Slf4j
@Component
public class UserAccessTokenFilter implements Filter {

    @Autowired
    private APIService apiService;
    @Autowired
    private SSOService ssoService;
    @Value("${tip.host}")
    private String tipHost;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
	    log.info("tokenfilter init");
    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)resp;
	    //获取user_access_token
        String user_access_token =  request.getParameter("user_access_token");
        //获取id_token
        String id_token = request.getParameter("id_token");
        HttpSession session = request.getSession();
        //设置session 过期时间
        session.setMaxInactiveInterval(36000);
        String userAccessToken = (String)session.getAttribute("userAccessToken");
        String idToken = (String)session.getAttribute("idToken");
        if(StringUtils.isEmpty(user_access_token) && StringUtils.isEmpty(userAccessToken)){
            return;
        }
        if(StringUtils.isNotEmpty(user_access_token) && StringUtils.isEmpty(userAccessToken)){ // tac首次请求转发到spzn 携带user_access_token
            session.setAttribute("userAccessToken", user_access_token);
	        log.info("过滤器中的user_access_token: {}", user_access_token);
            //发送请求获取tiptoken
            apiService.getTipAccessToken(session);
        }
        String userToken = StringUtils.isEmpty(user_access_token)?userAccessToken:user_access_token;
        if(StringUtils.isEmpty(idToken)&&StringUtils.isEmpty(id_token)&&StringUtils.isNotEmpty(userToken)){
            StringBuffer requestPath = request.getRequestURL();
            if ( requestPath.toString().startsWith("http:")) {
                requestPath.insert('s',4);
            }
            String queryStr = request.getQueryString();
            if(StringUtils.isNotEmpty(queryStr)){
                queryStr = URLEncoder.encode(queryStr, "UTF-8");
                requestPath.append("?").append(queryStr);
            }
	        String redirectUrl = "https://"+ tipHost +"/enduser/sp/sso/policejwt18?enterpriseId" +
                    "=police&redirect_uri="+requestPath+"&user_access_token="+userToken;
	        log.info("重定向: {}",  redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }
        if(StringUtils.isNotEmpty(id_token) && StringUtils.isEmpty(idToken)){
            session.setAttribute("idToken", id_token);
	        log.info("过滤器中的id_token: {}" , id_token);
            //保存解析后的idToken
            try {
                DingdangUserRetriever.User resolveIdToken = ssoService.resolveIdToken(id_token);
                session.setAttribute("resolveIdToken", resolveIdToken);
                String uuid = resolveIdToken.getUdAccountUuid();
                String access_token = resolveIdToken.getAzp();
	            log.info("前置过滤器解析获得idToken的uuid:  {}",  uuid);
	            log.info("前置过滤器解析获得idToken的access_token: {}", access_token );
	            log.info("前置过滤器解析或得idToken的用户名:  {}", resolveIdToken.getUsername());
                log.info("ApplcationName: {}", resolveIdToken.getApplicationName());
                log.info("Email: {}", resolveIdToken.getEmail());
                log.info("ExternalId: {}",resolveIdToken.getExternalId());
                log.info("ExtendFielads:  {}", JSON.toJSONString(resolveIdToken.getExtendFields()));
                log.info("UserId: {}",  resolveIdToken.getUserId());
                log.info("PurchaseId: {}", resolveIdToken.getPurchaseId());
                log.info("ApplicationName: {}", resolveIdToken.getApplicationName());
                log.info("PhoneNumer: {}", resolveIdToken.getPhoneNumber());
                log.info("Mobile: {}", resolveIdToken.getMobile());
                log.info("Name: {}", resolveIdToken.getName());
                //设置cookies
                log.info("cookie信息保存暂无保存!");
                //log.info("sessionid: {}",request.getSession());
	            //解析成功，id_token 符合标准，向后置去发送（保证tipToken 已经获取成功）
                //tipToken过期处理
                //apiService.reTipToken(session);
	            log.info("过滤器中session信息: " + session.getId());
            } catch (JoseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        /*if (request.getRequestURI().equals("/spzn/logout")) {
            log.info("系统准备登出...");
            DingdangUserRetriever.User resolveIdToken = (DingdangUserRetriever.User) session.getAttribute("resolveIdToken");
            String url = "https://" + tipHost + "/api/public/bff/v1/isv/hzos_isv_logout";
            url = url + "?sp_application_session_id=" +
                    resolveIdToken.getExtendFields().get("sp_application_session_id")+
                    "&appName="+ resolveIdToken.getApplicationName()+"&purchaseId="+
                    resolveIdToken.getPurchaseId()+"&logoutType=logout";
            log.info("appName: {}",resolveIdToken.getApplicationName());
            log.info("url: {}", url);
            log.info("系统登出成功!");
            response.sendRedirect(url);
            return;
        }*/
        /*if (request.getSession(false) == null) {
            log.info("session已失效!");
            Cookie[] cookies = request.getCookies();
            String url = "http://" + tipHost + "/api/public/bff/v1/isv/hzos_isv_logout?";
            for (Cookie cookie: cookies) {
                if (cookie.getName().equals("sp_application_session_id")){
                    url += "sp_application_session_id=" + cookie.getValue()+"&";
                    log.info("sp_applcation_session_id: {}",cookie.getValue());
                }
                if (cookie.getName().equalsIgnoreCase("appName")){
                    url += "appName=" + cookie.getValue()+"&";
                    log.info("appName: {}",cookie.getValue());
                }
                if (cookie.getName().equalsIgnoreCase("purchaseId")){
                    url += "purchaseId=" + cookie.getValue()+"&";
                    log.info("purchaseId: {}",cookie.getValue());
                }
            }
            url += "logoutType=timeOut";
            log.info("超时登出url: {}", url);
            response.sendRedirect(url);
            return;
        }*/
        chain.doFilter(req,resp);
    }
    @Override
    public void destroy() {
	    log.info("tokenfilter destroy");
    }

}