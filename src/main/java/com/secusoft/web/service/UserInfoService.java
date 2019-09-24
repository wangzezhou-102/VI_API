package com.secusoft.web.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.model.ResultVo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wangzezhou
 * @since 2019/7/30
 */
public interface UserInfoService {
    //获取权限系统列表
    ResultVo getPermissionList();
    //获取用户详细信息
    ResultVo getUserDetailInfo(DingdangUserRetriever.User resolveIdToken);
    //获取指定用户的角色信息
    ResultVo getUserRoleInfo();
    //获取所有的角色信息
    ResultVo getRoleList();
    //获取角色已经关联的账户列表
    ResultVo getRoleMemberList();
    //获取根组织机构
    ResultVo getRootOrg();
    //获取指定组织机构下的组织结构列表
    ResultVo getOrgList();
    //获取指定组织结构详情
    ResultVo getOrgDetailInfo();
    //获取指定组织机构下的用户列表
    ResultVo getOrgUserList();
    //菜单权限
    ResultVo getMenuPermissions(HttpServletRequest request);
    //获取用户角色和权限
    ResultVo getRolePermissions (HttpServletRequest request);
    //获取用户权限系统ps_access_token
    JSONObject getPsAccessToken();
}
