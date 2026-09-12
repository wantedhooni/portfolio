package com.revy.springbatchquartz.alert;

import com.revy.springbatchquartz.config.AppProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 이메일 알림을 시뮬레이션하는 서비스.
 */
@Service
public class EmailAlertService implements AlertService {

    private static final Logger log = LoggerFactory.getLogger(EmailAlertService.class);
    private final AppProperties appProperties;

    /**
     * 이메일 알림 서비스를 생성한다.
     *
     * @param appProperties 애플리케이션 설정
     */
    public EmailAlertService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    /**
     * 이메일 알림 내용을 로그로 출력한다.
     *
     * @param title 알림 제목
     * @param message 알림 내용
     */
    @Override
    public void sendAlert(String title, String message) {
        log.warn("[이메일 알림] 대상: {}, 제목: {}, 내용: {}",
            appProperties.getAlert().getEmailTo(), title, message);
    }
}
