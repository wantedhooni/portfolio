package com.example.spring_batch_demo;

import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MemberCsvReader {

    @Bean
    public ItemReader<Member> memberItemReader(
            @Value("classpath:members.csv") Resource resource) {
        return new FlatFileItemReaderBuilder<Member>()
                .name("memberItemReader")
                .resource(resource)
                .delimited()
                .names("name", "email", "age")
                .fieldSetMapper(fieldSet -> {
                    Member member = new Member();
                    member.setName(fieldSet.readString("name"));
                    member.setEmail(fieldSet.readString("email"));
                    member.setAge(fieldSet.readInt("age"));
                    member.setCreatedAt(LocalDateTime.now());
                    return member;
                })
                .build();
    }
}
