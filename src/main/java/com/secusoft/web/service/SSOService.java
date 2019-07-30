package com.secusoft.web.service;

import com.secusoft.web.model.ResultVo;
import org.jose4j.lang.JoseException;

import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * @author wangzezhou
 * @date 2019-07-19
 */
public interface SSOService {
    //获取用户详细信息
    ResultVo getUserDetailInfo(HttpSession session)throws JoseException, IOException;
    //获取idToken
    void getIdToken(HttpSession session);
}
