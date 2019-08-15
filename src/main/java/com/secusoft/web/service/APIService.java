package com.secusoft.web.service;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.model.ResultVo;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public interface APIService {
    //业务API对接
    ResultVo requestAPI(JSONObject jsonObject, HttpServletRequest request);
    //获取图片
    void requestAPIURL(HttpServletRequest request, HttpServletResponse response);
    //获取TIP 访问令牌
    void getTipAccessToken(HttpSession session);
    //重新获取TIP 访问令牌
    void requestTipToken(HttpSession session);
    // 通过TIP得到后置访问图片
    void requestImage(HttpServletRequest request, HttpServletResponse response);
}
