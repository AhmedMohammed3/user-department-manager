package com.learn.microservices.apigateway.config;

import com.learn.microservices.apigateway.filter.AuthFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class GatewayConfig {

    final AuthFilter authFilter;

    public GatewayConfig(AuthFilter authFilter) {
        this.authFilter = authFilter;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {

        return builder.routes()
                .route("user-service", r -> r
                        .path("/users/**")
                        .filters(f -> f.filters(authFilter))
                        .uri("lb://user-service"))
                .route("department-service", r -> r
                        .path("/departments/**")
                        .filters(f -> f.filters(authFilter))
                        .uri("lb://department-service"))

                .route("auth-server", r -> r
                        .path("/auth/**")
                        .uri("lb://auth-service"))
                .build();
    }

    @Bean
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
