package com.store.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingGlobalFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();
        
        log.info("[GATEWAY-REQUEST] Incoming {} request to {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            var statusCode = exchange.getResponse().getStatusCode();
            log.info("[GATEWAY-RESPONSE] Completed {} request to {} with status: {}", method, path, statusCode);
        }));
    }

    @Override
    public int getOrder() {
        return -1; // Highest precedence logging
    }
}