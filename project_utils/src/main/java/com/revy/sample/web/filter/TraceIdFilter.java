package com.revy.sample.web.filter;


import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * X-Trace-Id 체크 / 생성 필터
 *
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String traceId = request.getHeader(HEADER_NAME);
        if(StringUtils.isBlank(traceId)){
            traceId = UuidCreator.getTimeOrderedEpoch().toString();
        }
        MDC.put("traceId", traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove("traceId");
        }
    }
}
