package com.secusoft.web.service;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.model.ResultVo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public interface APIService {
    //业务API 对接
    ResultVo requestAPI(JSONObject jsonObject, HttpServletRequest request);
    //获取图片
    void requestAPI(HttpServletRequest request, HttpServletResponse response);
    //获取TIP 访问令牌
    ResultVo getTipAccessToken(HttpSession session);
    //重新获取TIP 访问令牌
    void requestTipToken(HttpSession session);
   
}
