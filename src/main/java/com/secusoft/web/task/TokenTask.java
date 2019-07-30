package com.secusoft.web.task;

import com.secusoft.web.core.util.SpringContextHolder;
import com.secusoft.web.service.APIService;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * 重新获取tipToken定时任务
 *
 * @author wangzezhou
 * @since 2019/07/29
 */
public class TokenTask implements Job {

    private static Logger log = LoggerFactory.getLogger(TokenTask.class);

    public TokenTask(){}
    @Resource
    public APIService apiService;
    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        JobDataMap patrolMap = jec.getJobDetail().getJobDataMap();
        HttpSession session = (HttpSession) patrolMap.get("params");
        log.info("获取新的tipToken：" );
        apiService.getTipAccessToken(session);
        log.info("获取tipToken成功：");
    }
}
