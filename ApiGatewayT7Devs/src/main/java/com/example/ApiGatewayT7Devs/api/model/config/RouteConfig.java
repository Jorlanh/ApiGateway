package com.example.ApiGatewayT7Devs.api.model.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração das rotas do API Gateway.
 */
@Configuration
public class RouteConfig {

    private final KeyResolver userKeyResolver;

    public RouteConfig(@Qualifier("userKeyResolver") KeyResolver userKeyResolver) {
        this.userKeyResolver = userKeyResolver;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Rota para o serviço de usuários
                .route("user-service", r -> r.path("/users/**")
                        .filters(f -> f.requestRateLimiter()
                                .configure(c -> c.setKeyResolver(userKeyResolver)))
                        .uri("lb://USER-SERVICE"))
                // Rota para o serviço de ordens
                .route("order-service", r -> r.path("/orders/**")
                        .filters(f -> f.requestRateLimiter()
                                .configure(c -> c.setKeyResolver(userKeyResolver)))
                        .uri("lb://ORDER-SERVICE"))
                .build();
    }
}