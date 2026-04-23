package com.example.spring_batch_demo;

import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class MemberItemProcessor implements ItemProcessor<Member, Member> {

    @Override
    public Member process(Member member) throws Exception {
        // 이메일 유효성 검증
        if (!isValidEmail(member.getEmail())) {
            throw new Exception("Invalid email: " + member.getEmail());
        }

        // 나이 검증
        if (member.getAge() < 18 || member.getAge() > 120) {
            return null;  // null 반환 시 Writer에서 제외
        }

        // 데이터 변환
        member.setName(member.getName().toUpperCase());

        return member;
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
}
