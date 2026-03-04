package com.revy.sampleredisson.config;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.redisson")
public class RedissonProperties {

    private String address;

    private String password;

    private int connectTimeout = 10000;

    private int operationTimeout = 3000;

    
}
