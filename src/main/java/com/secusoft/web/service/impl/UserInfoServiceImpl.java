package com.secusoft.web.service.impl;

import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.exception.BizExceptionEnum;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserInfoServiceImpl implements UserInfoService {
    @Value("${tip.host}")
    private String tipHost = "tap.hzgaaqfwpt.hzs.zj";

    //获取权限系统列表
    public ResultVo getPermissionList(){
        return null;
    }

    //获取用户详细信息
    public ResultVo getUserDetailInfo(DingdangUserRetriever.User resolveIdToken){
        log.info("获取用户信息请求开始...");
        HttpGet get = null;
        //处理请求路径
        try {
            //HttpClient有很多，可以根据个人喜好选用
            HttpClient httpClient = HttpClients.createDefault();
            String geturl = "http://" + tipHost +"/api/bff/v1/user/detail/" + resolveIdToken.getUdAccountUuid() +
                    "?access_token=" + resolveIdToken.getAzp();
            //根据http实际方法，构造HttpPost，HttpGet，HttpPut等
            get = new HttpGet(geturl);
            log.info("请求用户信息的完整路径： {}" , geturl);
            // 发送http请求
            HttpResponse response = httpClient.execute(get);
            int statusCode = response.getStatusLine().getStatusCode();
            HttpEntity entity1 = response.getEntity();
            String resultStr = EntityUtils.toString(entity1);
            //返回结果
            return ResultVo.success(resultStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (get != null) {
                try {//断开链接
                    get.releaseConnection();
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return ResultVo.failure(BizExceptionEnum.SERVER_ERROR);
    }
    //获取指定用户的角色信息
    public ResultVo getUserRoleInfo(){
        return null;
    }
    //获取所有的角色信息
    public ResultVo getRoleList(){
        return null;
    }
    //获取角色已经关联的账户列表
    public ResultVo getRoleMemberList(){
        return null;
    }
    //获取根组织机构
    public ResultVo getRootOrg(){
        return null;
    }
    //获取指定组织机构下的组织结构列表
    public ResultVo getOrgList(){
        return null;
    }
    //获取指定组织结构详情
    public ResultVo getOrgDetailInfo(){
        return null;
    }
    //获取指定组织机构下的用户列表
    public ResultVo getOrgUserList(){
        return null;
    }

}
