package com.example.bulk_test_sample.scheduler;

import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;

/**
 * Quartz Job에 Spring Bean 자동 주입을 제공한다.
 */
public class AutowiringSpringBeanJobFactory extends SpringBeanJobFactory {

    private final AutowireCapableBeanFactory beanFactory;

    /**
     * 자동 주입 팩토리를 생성한다.
     *
     * @param beanFactory 스프링 빈 팩토리
     */
    public AutowiringSpringBeanJobFactory(AutowireCapableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    /**
     * Job 인스턴스를 생성하고 자동 주입을 수행한다.
     *
     * @param bundle Quartz 번들
     * @return Job 인스턴스
     * @throws Exception 생성 실패
     */
    @Override
    protected Object createJobInstance(TriggerFiredBundle bundle) throws Exception {
        Object job = super.createJobInstance(bundle);
        beanFactory.autowireBean(job);
        return job;
    }
}
