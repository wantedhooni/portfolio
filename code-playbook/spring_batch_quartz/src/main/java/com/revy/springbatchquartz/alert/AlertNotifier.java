package com.revy.springbatchquartz.alert;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * 여러 알림 채널로 동시에 전송하는 서비스.
 */
@Service
public class AlertNotifier {

    private final List<AlertService> alertServices;

    /**
     * 알림 채널 목록을 주입받아 초기화한다.
     *
     * @param alertServices 알림 서비스 목록
     */
    public AlertNotifier(List<AlertService> alertServices) {
        this.alertServices = alertServices;
    }

    /**
     * 모든 알림 채널에 메시지를 전송한다.
     *
     * @param title 알림 제목
     * @param message 알림 내용
     */
    public void notifyAll(String title, String message) {
        alertServices.forEach(service -> service.sendAlert(title, message));
    }
}
