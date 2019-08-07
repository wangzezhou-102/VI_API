package com.secusoft.web.core.filter;

import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.SSOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class UserAccessTokenFilter implements Filter {

    //指纹生成
    private FingerTookit fingerTookit;
    @Autowired
    private APIService apiService;
    @Autowired
    private SSOService ssoService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("useraccesstokenfilter init");
    }
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest)req;
        //获取user_access_token
        String userAccessToken =  request.getParameter("user_access_token");
        HttpSession session = request.getSession();
        if(userAccessToken != null && userAccessToken.length() > 0) {
            //保存user_access_token供申请TIP令牌使用
            session.setAttribute("userAccessToken", userAccessToken);
            System.out.println("tap访问令牌：" + userAccessToken);
            //获取idToken
            //ssoService.getIdToken(session);
            //发送请求获取tip token
            apiService.getTipAccessToken(session);
            //tipToken过期处理
            //apiService.requestTipToken(session);
        }
        //向后置发送idToken
        //ssoService.sendIdToken(request);
        chain.doFilter(req,resp);
    }
    @Override
    public void destroy() {
        System.out.println("useraccesstokenfilter destroy");
    }

}