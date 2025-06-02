package com.example.ApiGatewayT7Devs.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class AggregationController {

    @Autowired
    private WebClient webClient;

    @GetMapping("/aggregate")
    public Mono<String> aggregateData() {
        return webClient.get()
                .uri("lb://USER-SERVICE/users")
                .retrieve()
                .bodyToMono(String.class);
    }
}