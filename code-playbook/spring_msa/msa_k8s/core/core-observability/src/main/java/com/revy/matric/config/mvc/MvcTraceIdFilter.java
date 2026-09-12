package com.revy.matric.config.mvc;

import com.revy.matric.config.MatricSupport;
import com.revy.matric.utils.ClientIpResolver;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.revy.matric.config.MatricSupport.MDC_CLIENT_IP;
import static com.revy.matric.config.MatricSupport.MDC_REQUEST_URL;

@Slf4j
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.HIGHEST_PRECEDENCE)
class MvcTraceIdFilter extends OncePerRequestFilter {

    private final Tracer tracer;

    MvcTraceIdFilter(Tracer tracer) {
        this.tracer = tracer;
        log.debug("MvcTraceIdFilter initialized");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        TraceContext context = this.tracer.currentTraceContext().context();
        if (context != null) {
            String traceId = context.traceId();
            response.setHeader(MatricSupport.TRACE_ID_HEADER, traceId);
        }
        Span span = this.tracer.currentSpan();
        if (span != null) {
            String requestUrl = request.getRequestURI();
            String clientIp = ClientIpResolver.resolveIpv4(request);
            log.debug("Tracing request URL: {}, clientIp:{}", requestUrl, clientIp);

            span.tag(MatricSupport.MDC_REQUEST_URL, requestUrl);
            span.tag(MatricSupport.MDC_CLIENT_IP, clientIp);

        }
        filterChain.doFilter(request, response);
    }


}
