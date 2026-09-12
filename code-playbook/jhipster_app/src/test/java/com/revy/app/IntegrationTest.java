package com.revy.app;

import com.revy.app.config.AsyncSyncConfiguration;
import com.revy.app.config.DatabaseTestcontainer;
import com.revy.app.config.JacksonConfiguration;
import com.revy.app.config.RedisTestContainer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;

/**
 * Base composite annotation for integration tests.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(
    classes = {
        JhipsterAppApp.class,
        JacksonConfiguration.class,
        AsyncSyncConfiguration.class,
        com.revy.app.config.JacksonHibernateConfiguration.class,
    }
)
@ImportTestcontainers({ DatabaseTestcontainer.class, RedisTestContainer.class })
public @interface IntegrationTest {}
