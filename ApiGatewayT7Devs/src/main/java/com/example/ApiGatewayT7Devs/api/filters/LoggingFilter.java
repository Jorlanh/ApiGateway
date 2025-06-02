package com.example.ApiGatewayT7Devs.api.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (config.isEnabled()) {
                System.out.println("Request: " + exchange.getRequest().getURI());
                if (config.isIncludeHeaders()) {
                    System.out.println("Headers: " + exchange.getRequest().getHeaders());
                }
            }
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isEnabled()) {
                    System.out.println("Response: " + exchange.getResponse().getStatusCode());
                    if (config.isIncludeHeaders()) {
                        // Nota: Os headers de resposta podem não estar disponíveis aq devido à natureza reativa
                        System.out.println("Response Headers: N/A (reactive context)");
                    }
                }
            }));
        };
    }

    public static class Config {
        private boolean enabled = true; // Habilitado por padrão
        private boolean includeHeaders = false; // Não incluir headers

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isIncludeHeaders() {
            return includeHeaders;
        }

        public void setIncludeHeaders(boolean includeHeaders) {
            this.includeHeaders = includeHeaders;
        }
    }
}