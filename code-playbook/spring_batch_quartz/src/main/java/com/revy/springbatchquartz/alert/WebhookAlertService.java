package com.revy.springbatchquartz.alert;

import com.revy.springbatchquartz.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 일반 웹훅 알림을 시뮬레이션하는 서비스.
 */
@Service
public class WebhookAlertService implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(WebhookAlertService.class);
    private final AppProperties appProperties;

    /**
     * 웹훅 알림 서비스를 생성한다.
     *
     * @param appProperties 애플리케이션 설정
     */
    public WebhookAlertService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 웹훅 알림 내용을 로그로 출력한다.
     *
     * @param title 알림 제목
     * @param message 알림 내용
     */
    @Override
    public void sendAlert(String title, String message) {
        log.warn("[웹훅 알림] URL: {}, 제목: {}, 내용: {}",
            appProperties.getAlert().getWebhookUrl(), title, message);
    }
}
