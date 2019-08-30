package com.secusoft.web.service;

import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.model.ResultVo;
import org.jose4j.lang.JoseException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author wangzezhou
 * @date 2019-07-19
 */
public interface SSOService {
   /* //获取用户详细信息
    ResultVo getUserDetailInfo(HttpSession session)throws JoseException, IOException;
  */
   //获取idToken
    void getIdToken(HttpSession session);
    //向后置发送idToken
    void sendIdToken(HttpServletRequest request);
    //解析idToken
    DingdangUserRetriever.User resolveIdToken(String idToken)throws JoseException, IOException;
    //应用系统登出
    String logout(HttpServletRequest request,HttpServletResponse response);
}
