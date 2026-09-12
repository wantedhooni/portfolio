package com.revy.springbatchquartz.batch;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

/**
 * 아이템 처리 중 일시적인 실패를 시뮬레이션하는 프로세서.
 */
@Slf4j
public class SampleItemProcessor implements ItemProcessor<String, String> {

    private final Map<String, Integer> retryCounts = new ConcurrentHashMap<>();

    /**
     * 문자열을 처리하며 특정 아이템에 대해서는 1회 실패를 발생시킨다.
     *
     * @param item 입력 아이템
     * @return 처리된 아이템
     */
    @Override
    public String process(String item) {
        log.info("item: {}", item);
        if ("retry".equalsIgnoreCase(item)) {
            int attempt = retryCounts.merge(item, 1, Integer::sum);
            if (attempt == 1) {
                throw new IllegalStateException("재시도 데모를 위한 임시 실패 발생");
            }
        }
        return item.toUpperCase();
    }
}
