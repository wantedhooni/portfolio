package com.revy.example.core.filter;

import com.revy.example.common.utils.UuidUtil;
import com.revy.example.core.utils.ClientIpResolver;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestTraceFilter extends OncePerRequestFilter {
    public static final String MDC_KEY_TRACE_ID = "traceId";
    public static final String MDC_REQUEST_URL = "requestUrl";
    public static final String MDC_CLIENT_IP = "clientIp";

    /**
     * RequestTraceFilter는 모든 HTTP 요청에 대해 실행되는 필터입니다. 이 필터는 요청의 URI, HTTP 메서드, 그리고 요청이 시작된 시간을 로그로 기록할 수 있습니다.
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        UUID traceID = UuidUtil.getTimeOrderedEpochUuidV7();
        String requestUri = request.getRequestURI();
        String clientIp = ClientIpResolver.resolveIpv4(request);
        MDC.put(MDC_KEY_TRACE_ID, traceID.toString());
        MDC.put(MDC_REQUEST_URL, requestUri);
        MDC.put(MDC_CLIENT_IP, clientIp);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
