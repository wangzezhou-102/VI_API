package com.secusoft.web.ssoapi;

import com.alibaba.cloudapi.sdk.core.BaseApiClient;
import com.alibaba.cloudapi.sdk.core.BaseApiClientBuilder;
import com.alibaba.cloudapi.sdk.core.annotation.NotThreadSafe;
import com.alibaba.cloudapi.sdk.core.annotation.ThreadSafe;
import com.alibaba.cloudapi.sdk.core.enums.Method;
import com.alibaba.cloudapi.sdk.core.enums.ParamPosition;
import com.alibaba.cloudapi.sdk.core.enums.Scheme;
import com.alibaba.cloudapi.sdk.core.model.ApiRequest;
import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import com.alibaba.cloudapi.sdk.core.model.BuilderParams;

import java.util.Map;

@ThreadSafe
public class SyncApiListClient extends BaseApiClient {

    public final static String GROUP_HOST = "17401b66a57547a7a48c58024537beeb.apigateway.cn-deqing-zjzfy01-zjga01.hzs.zj";

    public SyncApiListClient(BuilderParams builderParams){  super(builderParams); }
    @NotThreadSafe
    public static class Builder extends BaseApiClientBuilder<Builder, SyncApiListClient>{
        @Override
        protected SyncApiListClient build(BuilderParams params){
            return new SyncApiListClient(params);
        }
    }

    public static Builder newBuilder(){
        return new Builder();
    }

    public static SyncApiListClient getInstance(){
        return getApiClassInstance(SyncApiListClient.class);
    }
    //
    public ApiResponse APIList(Map<String, String> queryParams, Map<String,String> headerParams,String apiPath) {
        ApiRequest apiRequest = new ApiRequest(Scheme.HTTP, Method.GET, GROUP_HOST, apiPath);
        if(queryParams != null){
            apiRequest.addMappedParams(queryParams, ParamPosition.QUERY);
        }
        if(headerParams != null){
            apiRequest.addMappedParams(headerParams, ParamPosition.HEADER);
        }
        return syncInvoke(apiRequest);
    }
    //
    public ApiResponse APIList(Map<String, String> queryParams, String apiPath,Map<String,String> formParams) {
        ApiRequest apiRequest = apiRequest = new ApiRequest(Scheme.HTTP, Method.POST_FORM, GROUP_HOST, apiPath);
        if(queryParams != null){
            apiRequest.addMappedParams(queryParams, ParamPosition.QUERY);
        }
        if(formParams != null){
            apiRequest.addMappedParams(formParams, ParamPosition.FORM);
        }
        return syncInvoke(apiRequest);
    }
    //有form(post)
    public ApiResponse APIList(Map<String, String> queryParams, Map<String,String> headerParams,Map<String,String> formParams,String apiPath) {
        ApiRequest apiRequest = new ApiRequest(Scheme.HTTP, Method.POST_FORM, GROUP_HOST, apiPath);
        if(queryParams != null){
            apiRequest.addMappedParams(queryParams, ParamPosition.QUERY);
        }
        if(headerParams != null){
            apiRequest.addMappedParams(headerParams, ParamPosition.HEADER);
        }
        if(formParams != null){
            apiRequest.addMappedParams(formParams, ParamPosition.FORM);
        }
        return syncInvoke(apiRequest);
    }
    //有body(post)
    public ApiResponse APIList(Map<String, String> queryParams, Map<String,String> headerParams,byte[] body,String apiPath) {
        ApiRequest apiRequest = null;
        if(body != null){
            apiRequest = new ApiRequest(Scheme.HTTP, Method.POST_BODY, GROUP_HOST, apiPath, body);
        }
        if(queryParams != null){
            apiRequest.addMappedParams(queryParams, ParamPosition.QUERY);
        }
        if(headerParams != null){
            apiRequest.addMappedParams(headerParams, ParamPosition.HEADER);
        }
        return syncInvoke(apiRequest);
    }

}
