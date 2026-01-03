package com.revy.springbatchquartz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 애플리케이션 커스텀 설정을 바인딩하는 프로퍼티 클래스.
 */
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private BatchProperties batch = new BatchProperties();
    private QuartzProperties quartz = new QuartzProperties();
    private AlertProperties alert = new AlertProperties();

    /**
     * 배치 관련 프로퍼티를 반환한다.
     *
     * @return 배치 프로퍼티
     */
    public BatchProperties getBatch() {
        return batch;
    }

    /**
     * 배치 관련 프로퍼티를 설정한다.
     *
     * @param batch 배치 프로퍼티
     */
    public void setBatch(BatchProperties batch) {
        this.batch = batch;
    }

    /**
     * 쿼츠 관련 프로퍼티를 반환한다.
     *
     * @return 쿼츠 프로퍼티
     */
    public QuartzProperties getQuartz() {
        return quartz;
    }

    /**
     * 쿼츠 관련 프로퍼티를 설정한다.
     *
     * @param quartz 쿼츠 프로퍼티
     */
    public void setQuartz(QuartzProperties quartz) {
        this.quartz = quartz;
    }

    /**
     * 알림 관련 프로퍼티를 반환한다.
     *
     * @return 알림 프로퍼티
     */
    public AlertProperties getAlert() {
        return alert;
    }

    /**
     * 알림 관련 프로퍼티를 설정한다.
     *
     * @param alert 알림 프로퍼티
     */
    public void setAlert(AlertProperties alert) {
        this.alert = alert;
    }

    /**
     * 배치 관련 설정 클래스.
     */
    public static class BatchProperties {
        private String jobName;
        private RetryProperties retry = new RetryProperties();

        /**
         * 배치 잡 이름을 반환한다.
         *
         * @return 잡 이름
         */
        public String getJobName() {
            return jobName;
        }

        /**
         * 배치 잡 이름을 설정한다.
         *
         * @param jobName 잡 이름
         */
        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        /**
         * 재시도 설정을 반환한다.
         *
         * @return 재시도 설정
         */
        public RetryProperties getRetry() {
            return retry;
        }

        /**
         * 재시도 설정을 설정한다.
         *
         * @param retry 재시도 설정
         */
        public void setRetry(RetryProperties retry) {
            this.retry = retry;
        }
    }

    /**
     * 재시도 관련 설정 클래스.
     */
    public static class RetryProperties {
        private int limit;
        private BackoffProperties backoff = new BackoffProperties();

        /**
         * 재시도 횟수를 반환한다.
         *
         * @return 재시도 횟수
         */
        public int getLimit() {
            return limit;
        }

        /**
         * 재시도 횟수를 설정한다.
         *
         * @param limit 재시도 횟수
         */
        public void setLimit(int limit) {
            this.limit = limit;
        }

        /**
         * 백오프 설정을 반환한다.
         *
         * @return 백오프 설정
         */
        public BackoffProperties getBackoff() {
            return backoff;
        }

        /**
         * 백오프 설정을 설정한다.
         *
         * @param backoff 백오프 설정
         */
        public void setBackoff(BackoffProperties backoff) {
            this.backoff = backoff;
        }
    }

    /**
     * 백오프 관련 설정 클래스.
     */
    public static class BackoffProperties {
        private long initial;
        private double multiplier;
        private long max;

        /**
         * 초기 백오프 시간을 반환한다.
         *
         * @return 초기 백오프
         */
        public long getInitial() {
            return initial;
        }

        /**
         * 초기 백오프 시간을 설정한다.
         *
         * @param initial 초기 백오프
         */
        public void setInitial(long initial) {
            this.initial = initial;
        }

        /**
         * 백오프 배수를 반환한다.
         *
         * @return 백오프 배수
         */
        public double getMultiplier() {
            return multiplier;
        }

        /**
         * 백오프 배수를 설정한다.
         *
         * @param multiplier 백오프 배수
         */
        public void setMultiplier(double multiplier) {
            this.multiplier = multiplier;
        }

        /**
         * 최대 백오프 시간을 반환한다.
         *
         * @return 최대 백오프
         */
        public long getMax() {
            return max;
        }

        /**
         * 최대 백오프 시간을 설정한다.
         *
         * @param max 최대 백오프
         */
        public void setMax(long max) {
            this.max = max;
        }
    }

    /**
     * 쿼츠 관련 설정 클래스.
     */
    public static class QuartzProperties {
        private String cron;

        /**
         * 크론 표현식을 반환한다.
         *
         * @return 크론 표현식
         */
        public String getCron() {
            return cron;
        }

        /**
         * 크론 표현식을 설정한다.
         *
         * @param cron 크론 표현식
         */
        public void setCron(String cron) {
            this.cron = cron;
        }
    }

    /**
     * 알림 관련 설정 클래스.
     */
    public static class AlertProperties {
        private String slackWebhookUrl;
        private String emailTo;
        private String webhookUrl;

        /**
         * 슬랙 웹훅 URL을 반환한다.
         *
         * @return 슬랙 웹훅 URL
         */
        public String getSlackWebhookUrl() {
            return slackWebhookUrl;
        }

        /**
         * 슬랙 웹훅 URL을 설정한다.
         *
         * @param slackWebhookUrl 슬랙 웹훅 URL
         */
        public void setSlackWebhookUrl(String slackWebhookUrl) {
            this.slackWebhookUrl = slackWebhookUrl;
        }

        /**
         * 이메일 수신자를 반환한다.
         *
         * @return 이메일 수신자
         */
        public String getEmailTo() {
            return emailTo;
        }

        /**
         * 이메일 수신자를 설정한다.
         *
         * @param emailTo 이메일 수신자
         */
        public void setEmailTo(String emailTo) {
            this.emailTo = emailTo;
        }

        /**
         * 웹훅 URL을 반환한다.
         *
         * @return 웹훅 URL
         */
        public String getWebhookUrl() {
            return webhookUrl;
        }

        /**
         * 웹훅 URL을 설정한다.
         *
         * @param webhookUrl 웹훅 URL
         */
        public void setWebhookUrl(String webhookUrl) {
            this.webhookUrl = webhookUrl;
        }
    }
}
