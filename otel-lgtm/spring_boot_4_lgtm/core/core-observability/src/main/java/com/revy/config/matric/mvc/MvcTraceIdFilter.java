package com.revy.config.matric.mvc;

import com.revy.config.matric.MatricSupport;
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
        TraceContext context = tracer.currentTraceContext().context();

        if (context != null) {
            String traceId = context.traceId();
            response.setHeader(MatricSupport.TRACE_ID_HEADER, traceId);
        }

        filterChain.doFilter(request, response);
    }




}
