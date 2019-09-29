package com.secusoft.web.core.listener;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@WebListener
public class SessionListener implements HttpSessionListener {

    //private AtomicInteger onLineCount = new AtomicInteger(0);

    public void sessionCreated(HttpSessionEvent event) {
        log.info("创建Session");
        //event.getSession().getServletContext().setAttribute("onLineCount", onLineCount.incrementAndGet());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        log.info("销毁Session");
        HttpSession session = event.getSession();
       /* session.removeAttribute("userAccessToken");
        session.removeAttribute("idToken");*/
        session.invalidate();

    }
}
