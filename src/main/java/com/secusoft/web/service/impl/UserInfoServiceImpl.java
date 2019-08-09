package com.secusoft.web.service.impl;

import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.UserInfoService;
import com.secusoft.web.ssoapi.SyncApiList;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserInfoServiceImpl implements UserInfoService {

    @Resource
    private SyncApiList syncApiList;

    //获取权限系统列表
    public ResultVo getPermissionList(){
        return null;
    }
    //获取用户详细信息
    public ResultVo getUserDetailInfo(DingdangUserRetriever.User resolveIdToken){
        //向it服务平台获取用户信息
        String apiPath = "/4a/api/bff/v1/user/detail";
        Map<String,String> queryParams = new HashMap<>();
        queryParams.put("uuid","c8d0e9db2baeb84c01e519efcb043eff1XAD1cmFF43");
        queryParams.put("access_token", "53069d1d-b505-43c3-aed1-2354bcfaf188");
        syncApiList.apiList(queryParams,null,apiPath);
        return null;
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
