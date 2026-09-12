package com.revy.springbatchquartz.alert;

/**
 * 배치 실패 알림을 전송하는 인터페이스.
 */
public interface AlertService {

    /**
     * 실패 메시지를 전달한다.
     *
     * @param title 알림 제목
     * @param message 알림 내용
     */
    void sendAlert(String title, String message);
}
