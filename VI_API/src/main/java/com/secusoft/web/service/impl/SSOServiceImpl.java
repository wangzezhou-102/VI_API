package com.secusoft.web.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.idsmanager.dingdang.jwt.DingdangUserRetriever;
import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.SSOService;
import com.secusoft.web.util.MyHttpClientPool;
import org.apache.http.entity.StringEntity;
import org.jose4j.lang.JoseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class SSOServiceImpl implements SSOService {
    @Value("${spzn.appid}")
    private String appid;
    @Value("${spzn.appkey}")
    private String appkey;
    @Value("${tip.url}")
    private String tipurl;
    private FingerTookit fingerTookit;

    //解析idToken
    public DingdangUserRetriever.User resolveIdToken(HttpSession session) throws JoseException, IOException {
        String publicKey = "{\"kty\":\"RSA\",\"kid\":\"1346614912326510837\",\"alg\":\"RS256\",\"n\":\"hOdf08cku1cEddGWHjOxalfqqmrMJ5LotXT28r0pgsw82uZiSNhi4kr1qVB7z3vUeqh0TffekWxsxGc0VXGoYrPYRkkS08old8CNZQjl7AbnY179kwPilburFuMXioYO55UgvXm2mpCBL8RKGiDSORlVXruBYhxGxZ8yAaloIPVZMTIBjhKtq_fc9K1fygjR7Q3BJJkDcLU92P1Jb8_EbpvRhkHzjKi-FcXbflPWY8dMQpksInp9c-AUByVvYQD3me94yVpyOcwVNUhT5sDUOHhbWjs0gkllY86GRqIHMpNk8VDI7BiXTny-etm7AGyU0_AJlwn4JcsERCqozH7n6w\",\"e\":\"AQAB\"}";
        String idToken = (String)session.getAttribute("idToken");
        if(StringUtils.isNotEmpty(idToken)){
            //解析id_token
            DingdangUserRetriever retriever = new DingdangUserRetriever(idToken, publicKey);
            DingdangUserRetriever.User token = retriever.retrieve();
            System.out.println("解析或得idToken的uuid:"+token.getUdAccountUuid());
            System.out.println("解析获得idToken的access_token:"+token.getAzp());
            System.out.println("idToken解析结束");
            return token;
        }
        return null;
    }
    //获取IdToken
    public void getIdToken(HttpSession session){
        System.out.println("开始获取idToken");
        String idToken = (String)session.getAttribute("idToken");
        if(StringUtils.isEmpty(idToken)){
            Map<String,String> map = new HashMap<>();
            map.put("enterpriseId","police");
            map.put("redirect_uri","http://spzn.hzgaaqfwpt.hzs.zj");
            String posturl = "http://tap.hzgaaqfwpt.hzs.zj:8081/enduser/sp/sso/policejwt18";
            StringEntity paramsEntity = new StringEntity(JSON.toJSONString(map),"UTF-8");
            String id_token = MyHttpClientPool.fetchByPostMethod(posturl, paramsEntity);
            if(!StringUtils.isEmpty(id_token)){
                System.out.println("通过TAP从4A获取到的id_token:"+ id_token);
                session.setAttribute("idToken",id_token);
                System.out.println("idToken保存成功!");
                System.out.println("开始解析idToken:");
                try{
                    DingdangUserRetriever.User user = resolveIdToken(session);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }else{
                System.out.println("通过TAP从4A获取到的id_token:"+ id_token);
            }
        }
    }
    //判断用户登录
    public ResultVo SSO(JSONObject jsonObject){
        //TODO
        //user_access_token(格式不确定,封装成bean,在session中获取进行判断)
        return ResultVo.success();
    }
    //获取用户详细信息
    public ResultVo getUserDetailInfo(HttpSession session)throws JoseException, IOException{
        DingdangUserRetriever.User token = resolveIdToken(session);
        System.out.println("IdToken信息");
        System.out.println("token的uuid:"+token.getUdAccountUuid());
        System.out.println("token的access_token:"+token.getAzp());
        return null;
    }


}
