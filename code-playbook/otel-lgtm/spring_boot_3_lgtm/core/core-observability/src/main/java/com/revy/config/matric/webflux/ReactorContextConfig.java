package com.revy.config.matric.webflux;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactorContextConfig {

    @PostConstruct
    void init() {
        Hooks.enableAutomaticContextPropagation();
    }
}