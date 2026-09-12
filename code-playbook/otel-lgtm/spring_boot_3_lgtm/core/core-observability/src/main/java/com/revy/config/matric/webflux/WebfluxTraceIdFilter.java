package com.revy.config.matric.webflux;

import com.revy.config.matric.MatricSupport;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.TraceContext;
import io.micrometer.tracing.Tracer;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebfluxTraceIdFilter implements WebFilter {

    private final Tracer tracer;

    WebfluxTraceIdFilter(Tracer tracer) {
        this.tracer = tracer;
        log.info("WebfluxTraceIdFilter initialized");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return Mono.defer(() -> {
            TraceContext context = tracer.currentTraceContext().context();
            if (context != null) {
                String traceId = context.traceId();
                exchange.getResponse()
                        .getHeaders()
                        .set(MatricSupport.TRACE_ID_HEADER, traceId);
            }

            return chain.filter(exchange);
        });
    }

}
