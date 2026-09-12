package com.revy.example.config.openapi;

import com.revy.example.idempotency.annotion.Idempotent;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import java.util.UUID;

@Component
public class IdempotentHeaderCustomizer implements OperationCustomizer {

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Idempotent annotation = handlerMethod.getMethodAnnotation(Idempotent.class);
        if (annotation != null) {
            // 2. 어노테이션에 설정된 헤더 이름과 설명을 기반으로 파라미터 추가
            operation.addParametersItem(new HeaderParameter()
                                                .name(annotation.headerName()) // 어노테이션의 headerName 값 사용
                                                .description(annotation.description())
                                                .required(true) // 필수값으로 설정
                                                .schema(new StringSchema().example(UUID.randomUUID())));
        }

        return operation;
    }
}
