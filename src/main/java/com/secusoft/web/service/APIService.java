package com.secusoft.web.service;

import com.secusoft.web.model.ResultVo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public interface APIService {
    //业务API对接
    ResultVo requestAPI(Object param, HttpServletRequest request);
    //获取图片
    void requestAPIURL(HttpServletRequest request, HttpServletResponse response);
    //获取TIP 访问令牌
    void getTipAccessToken(HttpSession session);
    //重新获取TIP 访问令牌
    void reTipToken(HttpSession session);
    // 通过TIP得到后置访问图片
    void requestImage(HttpServletRequest request, HttpServletResponse response);
    // 通过TIP得到后置文件
    void requestFile(HttpServletRequest request, HttpServletResponse response);
}
