package com.revy.example.init;

import com.revy.example.domain.Post;
import com.revy.example.repository.PostRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 애플리케이션 시작 시 대용량 샘플 데이터를 생성하는 초기화 컴포넌트.
 */
@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements ApplicationRunner {

    private static final int SAMPLE_COUNT = 3000;
    private final PostRepository postRepository;

    /**
     * 게시글 데이터가 비어 있을 때 3,000건의 샘플 데이터를 생성한다.
     *
     * @param args 애플리케이션 인자
     */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if(postRepository.count() > 0){
            return;
        }

        List<Post> posts = new ArrayList<>(SAMPLE_COUNT);
        IntStream.rangeClosed(1, SAMPLE_COUNT)
                 .forEach(index -> posts.add(new Post("샘플 제목 " + index, "샘플 내용 " + index)));

        postRepository.saveAll(posts);
    }
}
