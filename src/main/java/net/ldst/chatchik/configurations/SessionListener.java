package net.ldst.chatchik.configurations;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
@Slf4j
public class SessionListener implements HttpSessionListener, ServletContextListener {
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("session died");
    }
}
