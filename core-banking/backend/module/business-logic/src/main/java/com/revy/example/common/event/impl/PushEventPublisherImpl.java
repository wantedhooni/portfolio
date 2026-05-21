package com.revy.example.common.event.impl;

import com.revy.example.common.event.PushEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.reactive.TransactionalEventPublisher;

@Component
public class PushEventPublisherImpl implements PushEventPublisher {
    private final ApplicationEventPublisher  publisher;

    public PushEventPublisherImpl(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

        public void PushPublish(Object event) {
            publisher.publishEvent(event);
        }


}
