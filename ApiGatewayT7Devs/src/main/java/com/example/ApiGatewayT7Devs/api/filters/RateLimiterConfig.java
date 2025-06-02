package com.example.ApiGatewayT7Devs.api.filters;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Configuração de um KeyResolver para o rate limiting no API Gateway.
 */
@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // Usa o endereço IP do cliente (mais confiável para rate limiting)
            String clientKey = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
            // Alternativa: usar o nome do host (se disponível)
            // String clientKey = exchange.getRequest().getRemoteAddress().getHostName();
            return Mono.just(clientKey != null ? clientKey : "unknown");
        };
    }
}