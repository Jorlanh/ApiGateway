package com.example.ApiGatewayT7Devs.api.filters;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Resolver para identificar a chave do usuário para o rate limiting.
 * Usa o IP do cliente como chave.
 */
@Component("userKeyResolver")
public class UserKeyResolver implements KeyResolver {

    @Override
    public Mono<String> resolve(org.springframework.web.server.ServerWebExchange exchange) {
        String clientIp = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        return Mono.just(clientIp != null ? clientIp : "unknown");
    }
}