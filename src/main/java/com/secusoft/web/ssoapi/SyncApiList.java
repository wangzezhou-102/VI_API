package com.secusoft.web.ssoapi;

import com.alibaba.cloudapi.sdk.core.model.ApiResponse;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
* 通过client 同步调用API
* */
@Component
public class SyncApiList {
    private static final String APP_KEY = "1561786123277138";

    private static final String APP_SECRET ="12f24da8de9c4029b5a0726e054b60ab";

    private SyncApiListClient syncClient = null;

    public SyncApiList(){
        this.syncClient = SyncApiListClient.newBuilder()
                .appKey(APP_KEY)
                .appSecret(APP_SECRET)
                .build();
    }

    public void apiList(Map<String, String> queryParams,Map<String,String> headerParams,Map<String, String> formParams,String apiPath){
        ApiResponse apiResponse = syncClient.APIList(queryParams, headerParams, formParams, apiPath);
        printResponse(apiResponse);
    }
    public void apiList(Map<String, String> queryParams,String apiPath,Map<String,String> formParams){
        ApiResponse apiResponse = syncClient.APIList(queryParams, formParams, apiPath);
        printResponse(apiResponse);
    }
    public void apiList(Map<String, String> queryParams,Map<String,String> headerParams,byte[] body,String apiPath){
        ApiResponse apiResponse = syncClient.APIList(queryParams, headerParams, body, apiPath);
        printResponse(apiResponse);
    }
    public void apiList(Map<String, String> queryParams,Map<String,String> headerParams,String apiPath){
        ApiResponse apiResponse = syncClient.APIList(queryParams, headerParams, apiPath);
        printResponse(apiResponse);
    }

    private static void printResponse(ApiResponse apiResponse){
        try{
            System.out.println("response code  =  " + apiResponse.getStatusCode());
            System.out.println("response content  =  " + new String(apiResponse.getBody(),"utf-8"));
            System.out.println("以上为响应数据展示!");
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
