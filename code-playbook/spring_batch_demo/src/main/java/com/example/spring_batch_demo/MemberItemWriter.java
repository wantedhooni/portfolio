package com.example.spring_batch_demo;

import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class MemberItemWriter implements ItemWriter<Member> {
    private final MemberRepository memberRepository;

    public MemberItemWriter(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void write(Chunk<? extends Member> items) throws Exception {
        memberRepository.saveAll(items);
    }
}
