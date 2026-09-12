package com.revy.sample.spring;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.reactive.TransactionalEventPublisher;

@Component
@RequiredArgsConstructor
public class SampleComponent {
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public void testSample(){
        applicationEventPublisher.publishEvent("aaa");
    }
}
