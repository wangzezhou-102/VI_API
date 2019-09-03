package com.secusoft.web.task;

import com.secusoft.web.core.util.SpringContextHolder;
import com.secusoft.web.service.APIService;
import com.secusoft.web.service.impl.APIServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
/**
 * 重新获取tipToken定时任务
 *
 * @author wangzezhou
 * @since 2019/07/29
 */
@Slf4j
public class TokenTask implements Job {

    public TokenTask(){}

    public APIServiceImpl apiServiceImpl = new APIServiceImpl();
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JobDataMap map = jec.getJobDetail().getJobDataMap();
        HttpSession session = (HttpSession) map.get("params");
        log.info("session信息 : {}",session.getId());
        log.info("获取新的TipToken..." );
        //apiServiceImpl.getTipAccessToken(session);
        log.info("获取TipToken成功");
    }
}
