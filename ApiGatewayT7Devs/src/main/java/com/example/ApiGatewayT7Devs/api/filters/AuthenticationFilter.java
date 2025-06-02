package com.example.ApiGatewayT7Devs.api.filters;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Filtro de autenticação para validar tokens JWT em requisições ao API Gateway.
 */
@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Value("${api.gateway.authentication.secret-key}")
    private String secretKey;

    @Value("${api.gateway.authentication.issuer}")
    private String issuer;

    @Value("${api.gateway.authentication.expiration}")
    private long expiration;

    @Value("${authentication.filter.skip-paths:}") // Valor padrão vazio se não configurado
    private String skipPaths;

    @Value("${authentication.filter.custom-header-name:}") // Valor padrão vazio se não configurado
    private String customHeaderName;

    @Value("${authentication.filter.custom-header-value:}") // Valor padrão vazio se não configurado
    private String customHeaderValue;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // Preenche o config com valores das propriedades ou usa os padrões
        if (skipPaths != null && !skipPaths.isEmpty()) {
            config.setSkipPaths(Arrays.asList(skipPaths.split(",")));
        } else {
            config.setSkipPaths(Collections.emptyList()); // Valor padrão explícito
        }
        config.setCustomHeaderName(customHeaderName != null && !customHeaderName.isEmpty() ? customHeaderName : "X-Custom-Info");
        config.setCustomHeaderValue(customHeaderValue != null && !customHeaderValue.isEmpty() ? customHeaderValue : "default-user");

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Verifica se o caminho deve ser ignorado (suporta padrões como /public/**)
            if (config.getSkipPaths() != null) {
                boolean isSkipped = config.getSkipPaths().stream()
                        .anyMatch(pattern -> pathMatcher.match(pattern.trim(), path));
                if (isSkipped) {
                    return chain.filter(exchange);
                }
            }

            List<String> authHeader = request.getHeaders().get("Authorization");

            if (authHeader == null || authHeader.isEmpty() || !authHeader.get(0).startsWith("Bearer ")) {
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            String token = authHeader.get(0).replace("Bearer ", "");

            try {
                // Validação do token JWT
                Claims claims = Jwts.parser()
                        .setSigningKey(secretKey.getBytes())
                        .parseClaimsJws(token)
                        .getBody();

                // Verifica issuer
                if (!claims.getIssuer().equals(issuer)) {
                    System.out.println("Issuer inválido: esperado=" + issuer + ", recebido=" + claims.getIssuer());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Verifica expiração
                Date expirationDate = claims.getExpiration();
                if (expirationDate == null || expirationDate.before(new Date())) {
                    System.out.println("Token expirado: expiração=" + expirationDate);
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                }

                // Adiciona o token e o custom header ao request para uso downstream
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-Auth-Token", token)
                        .header(config.getCustomHeaderName(), config.getCustomHeaderValue())
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            } catch (Exception e) {
                System.out.println("Erro ao validar token: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    /**
     * Configurações do filtro de autenticação.
     */
    public static class Config {
        private List<String> skipPaths = Collections.emptyList(); // Valor padrão: lista vazia
        private String customHeaderName = "X-Custom-Info"; // Valor padrão
        private String customHeaderValue = "default-user"; // Valor padrão

        public List<String> getSkipPaths() {
            return skipPaths;
        }

        public void setSkipPaths(List<String> skipPaths) {
            this.skipPaths = skipPaths;
        }

        public String getCustomHeaderName() {
            return customHeaderName;
        }

        public void setCustomHeaderName(String customHeaderName) {
            this.customHeaderName = customHeaderName;
        }

        public String getCustomHeaderValue() {
            return customHeaderValue;
        }

        public void setCustomHeaderValue(String customHeaderValue) {
            this.customHeaderValue = customHeaderValue;
        }
    }
}