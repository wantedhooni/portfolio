package com.revy.example.idempotency.interceptor;

import com.revy.example.idempotency.annotion.Idempotent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String PREFIX_IDEMPOTENCY_KEY = "idempotency";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Idempotent idempotent = handlerMethod.getMethodAnnotation(Idempotent.class);

        if (idempotent == null) {
            return true;
        }

        String key = request.getHeader(idempotent.headerName());
        if (key == null || key.isBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing Idempotency-Key header");
            return false;
        }

        //TODO:revy -> 실제 운영 에서는 사용자 단위로 체크 하는게 편함 userId+Key
        /*
        String userId = "";
        String redisKey = String.format("%s:%s:%s", PREFIX_IDEMPOTENCY_KEY, userId, key);
         */

        String redisKey = String.format("%s:%s", PREFIX_IDEMPOTENCY_KEY, key);
        // setIfAbsent (SETNX)를 사용하여 원자적으로 체크 및 저장
        Boolean success = redisTemplate
                .opsForValue()
                .setIfAbsent(redisKey, "processing", idempotent.expireTime(), TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(success)) {
            response.sendError(HttpStatus.CONFLICT.value(), "Duplicate request detected");
            return false;
        }

        return HandlerInterceptor.super.preHandle(request, response, handler);
    }
}
