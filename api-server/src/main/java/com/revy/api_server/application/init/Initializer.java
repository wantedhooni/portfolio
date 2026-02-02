package com.revy.api_server.application.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class Initializer implements CommandLineRunner {

    /**
     * @param args incoming main method arguments
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        log.info("LOGSTASH.ENABLE: {}" , System.getProperty("LOGSTASH.ENABLE"));
    }
}
