package com.revy.example.admin.config;

import com.revy.example.core.config.AbstractOpenApiConfig;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig extends AbstractOpenApiConfig {

    @Override
    protected String getInfoVersion() {
        return "V1";
    }

    @Override
    protected String getDesc() {
        return "ADMIN-API 명세서";
    }

    @Override
    protected String getTitle() {
        return "ADMIN-API";
    }
}
