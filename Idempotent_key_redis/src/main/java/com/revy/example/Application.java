package com.revy.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import redis.embedded.RedisServer;

import java.io.IOException;


@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
        try {
            RedisServer redisServer = new RedisServer(6379);
            redisServer.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
