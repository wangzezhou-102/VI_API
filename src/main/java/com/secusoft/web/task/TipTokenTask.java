package com.secusoft.web.task;

import com.alibaba.fastjson.JSONObject;
import com.secusoft.web.core.support.FingerTookit;
import com.secusoft.web.core.util.StringUtils;
import com.secusoft.web.model.ResultVo;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.impl.APIServiceImpl;
import org.apache.catalina.session.StandardSessionFacade;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class TipTokenTask {
    private static final Logger log = LoggerFactory.getLogger(TipTokenTask.class);
    @Resource
    private APIService apiService;

    private static  HttpSession session ;
    //@Scheduled(cron="0/5 * * * * ?")
    public void syncGetTipToken() {
        log.info("重新获取tip访问令牌 start");
        apiService.getTipAccessToken(session);
        log.info("重新获取TIP访问令牌 end");
    }

}
