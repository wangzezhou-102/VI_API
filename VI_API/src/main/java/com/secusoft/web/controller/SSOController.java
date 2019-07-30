package com.secusoft.web.controller;

import com.secusoft.web.core.common.Constants;
import com.secusoft.web.core.util.ResponseUtil;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.SSOService;
import org.jose4j.lang.JoseException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 登录接口
 * @author wangzezhou
 * @date 2019-07-19
 */
@RestController
@CrossOrigin(value = "*", maxAge = 3600)
public class SSOController {
    @Resource
    private SSOService ssoService;
    //获取用户详细信息
    @PostMapping("/getUserDetailInfo")
    public ResponseEntity getUserDetailInfo(HttpSession session){
        ResultVo userDetailInfo = null;
        try{
            userDetailInfo = ssoService.getUserDetailInfo(session);
        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseUtil.handle(Constants.OK, userDetailInfo);
    }

}
