package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DingDingStreamRunner implements ApplicationRunner {
    @Autowired
    private DingDingStreamService dingDingStreamService;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        dingDingStreamService.registerRobotEvent();
    }
}
