package com.revy.example.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

public abstract class AbstractOpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        // 1. Info 설정
        Info info = new Info()
                .title(getTitle())
                .description(getDesc())
                .version(getInfoVersion());

        // 2. SecurityScheme 정의
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        // 3. 전역 보안 요구사항 설정
        SecurityRequirement securityRequirement = new SecurityRequirement()
                .addList(SECURITY_SCHEME_NAME);

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME, securityScheme));
    }

    protected abstract String getInfoVersion();

    protected abstract String getDesc();

    protected abstract String getTitle();


    @Slf4j
    @Component
    @RequiredArgsConstructor
    static class SpringDocUrlLogger {

        private final Environment env;

        @EventListener
        public void on(WebServerInitializedEvent event) {
            int port = event.getWebServer().getPort();

            String protocol = env.getProperty("server.ssl.enabled", Boolean.class, false)
                    ? "https"
                    : "http";

            String contextPath = env.getProperty("server.servlet.context-path", "");
            String swaggerPath = env.getProperty("springdoc.swagger-ui.path", "/swagger-ui/index.html");
            String apiDocsPath = env.getProperty("springdoc.api-docs.path", "/v3/api-docs");

            log.info("Swagger UI: {}://localhost:{}{}{}", protocol, port, contextPath, swaggerPath);
            log.info("OpenAPI Docs: {}://localhost:{}{}{}", protocol, port, contextPath, apiDocsPath);
        }
    }
}

