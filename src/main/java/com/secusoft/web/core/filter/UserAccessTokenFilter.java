package com.secusoft.web.core.filter;

import com.alibaba.fastjson.JSON;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.SSOService;
import com.secusoft.web.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
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
    @Autowired
    private UserInfoService userInfoService;
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
        response.setHeader("X-Frame-Options", "ALLOW-FROM");
	    //获取user_access_token
        String user_access_token =  request.getParameter("user_access_token");
        //获取id_token
        String id_token = request.getParameter("id_token");
        String requesttest = request.getParameter("requesttest");
        HttpSession session = null;
        if (request.getSession(false) == null) {
            session = request.getSession();
            log.info("创建新会话");
            //设置session 过期时间
            session.setMaxInactiveInterval(21600);
        } else {
            session = request.getSession();
        }
        log.info("过滤器中sessionid： {}", session.getId());
        //用户信息清空，重新登录
        String userAccessToken = (String)session.getAttribute("userAccessToken");
        String idToken = (String)session.getAttribute("idToken");

        if(StringUtils.isNotEmpty(user_access_token) && StringUtils.isEmpty(userAccessToken)){ // tac首次请求转发到spzn 携带user_access_token
            session.setAttribute("userAccessToken", user_access_token);
	        log.info("过滤器中的user_access_token:  {}", user_access_token);
            //发送请求获取tiptoken
            apiService.getTipAccessToken(session);
        }
        String userToken = StringUtils.isEmpty(user_access_token)?userAccessToken:user_access_token;
        if(StringUtils.isEmpty(idToken) && StringUtils.isEmpty(id_token) && StringUtils.isNotEmpty(userToken)){
            StringBuffer requestPath = request.getRequestURL();
            if ( requestPath.toString().startsWith("http:")) {
                requestPath.insert(4,'s');
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
	            log.info("前置过滤器解析获得idToken的用户名:  {}", resolveIdToken.getUsername());
                log.info("Sub: {}", resolveIdToken.getSub());
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
        log.info("session中userAccessToken :   {}", (String)session.getAttribute("userAccessToken"));
        log.info("session中resloveToken :   {}", session.getAttribute("resolveIdToken"));
        if ( StringUtils.isEmpty(user_access_token) && StringUtils.isEmpty(userAccessToken) && StringUtils.isEmpty(id_token) && StringUtils.isEmpty(idToken) && StringUtils.isEmpty(requesttest)) {
            log.info("用户信息过期，超时登出");
            log.info("过期新创建的 sessionid: {}", session.getId());
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json; charset=utf-8");
            ResultVo resultVo = new ResultVo();
            resultVo.setCode(610);
            resultVo.setMessage("会话超时");
            String responseJsonStr = JSON.toJSONString(resultVo);
            response.getWriter().write(responseJsonStr);
            return;
        }

        chain.doFilter(req,resp);
    }
    @Override
    public void destroy() {
	    log.info("tokenfilter destroy");
    }

}