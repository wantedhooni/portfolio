package com.revy.springbatchquartz.batch;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

/**
 * 처리된 아이템을 로그로 출력하는 라이터.
 */
public class SampleItemWriter implements ItemWriter<String> {

    private static final Logger log = LoggerFactory.getLogger(SampleItemWriter.class);

    /**
     * 전달받은 아이템 목록을 로그로 출력한다.
     *
     * @param chunk 처리된 아이템 목록
     */
    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        log.info("아이템 저장: {}", chunk);
    }
}
