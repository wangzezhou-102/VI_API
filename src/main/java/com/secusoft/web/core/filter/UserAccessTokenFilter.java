package com.secusoft.web.core.filter;

import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.SSOService;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
@Component
public class UserAccessTokenFilter implements Filter {

    @Autowired
    private APIService apiService;
    @Autowired
    private SSOService ssoService;

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
        session.setMaxInactiveInterval(-1);
        //System.out.println("session 运行时类：" + session.getClass().getName());
        String userAccessToken = (String)session.getAttribute("userAccessToken");
        String idToken = (String)session.getAttribute("idToken");
        if(StringUtils.isEmpty(user_access_token)&&StringUtils.isEmpty(userAccessToken)){
            return;
        }
        if(StringUtils.isNotEmpty(user_access_token) && StringUtils.isEmpty(userAccessToken)){ // tac首次请求转发到spzn 携带user_access_token
            session.setAttribute("userAccessToken", user_access_token);
            log.info("过滤器中的user_access_token:" + user_access_token);
            //发送请求获取tip token
            apiService.getTipAccessToken(session);
        }
        String userToken = StringUtils.isEmpty(user_access_token)?userAccessToken:user_access_token;
        if(StringUtils.isEmpty(idToken)&&StringUtils.isEmpty(id_token)&&StringUtils.isNotEmpty(userToken)){
            String redirectUrl = "http://tap.hzgaaqfwpt.hzs.zj:8081/enduser/sp/sso/policejwt18?enterpriseId" +
                    "=police&redirect_uri="+request.getRequestURL()+"&user_access_token="+userToken;
            log.info("重定向:" + redirectUrl);
            response.sendRedirect(redirectUrl);
            return;
        }
        if(StringUtils.isNotEmpty(id_token) && StringUtils.isEmpty(idToken)){
            session.setAttribute("idToken", id_token);
            log.info("过滤器中的id_token: " + id_token);
            //保存解析后的idToken
            try {
                DingdangUserRetriever.User resolveIdToken = ssoService.resolveIdToken(id_token);
                session.setAttribute("resolveIdToken", resolveIdToken);
                String uuid = resolveIdToken.getUdAccountUuid();
                String access_token = resolveIdToken.getAzp();
                log.info("前置过滤器解析获得idToken的uuid:" + uuid);
                log.info("前置过滤器解析获得idToken的access_token:" + access_token );
                log.info("前置过滤器解析或得IDToken的用户名:" + resolveIdToken.getUsername());
                log.info("idToken的过期时间:" + resolveIdToken.getExp());
                //解析成功，id_token 符合标准，向后置去发送（保证tipToken 已经获取成功）
                 ssoService.sendIdToken(request);
                //tipToken过期处理
                 //apiService.reTipToken(session);
                log.info("过滤器中session信息: " + session.getId());
            } catch (JoseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        chain.doFilter(req,resp);
    }
    @Override
    public void destroy() {
        log.info("tokenfilter destroy");
    }

}