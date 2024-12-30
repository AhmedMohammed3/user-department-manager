package com.learn.microservices.apigateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.learn.microservices.apigateway.model.User;
import com.learn.microservices.apigateway.util.AuthUtil;
import com.learn.microservices.apigateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Predicate;

@Component
@RefreshScope
public class AuthFilter implements GatewayFilter {

    private final Logger log = LoggerFactory.getLogger(AuthFilter.class);

    public static final List<String> unprotectedURLs = List.of("/login");

    public Predicate<ServerHttpRequest> isSecured =
            request -> unprotectedURLs.stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

    private final JwtUtil jwtUtil;

    private final AuthUtil authUtil;

    public AuthFilter(JwtUtil jwtUtil, AuthUtil authUtil) {
        this.jwtUtil = jwtUtil;
        this.authUtil = authUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (isSecured.test(request)) {
            log.info("validating authentication token");

            if (request.getHeaders().containsKey("Authorization")) {

                String token = request.getHeaders().get("Authorization").toString().split(" ")[1];

                try {
                    if (jwtUtil.isInvalid(token)) {
                        return onError(exchange, HttpStatus.UNAUTHORIZED);
                    }
                    log.info("Token from Authorization header is valid.");
                } catch (Exception e) {
                    log.warn("JWT validation error: {}", e.getMessage());
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }
            } else {
                if (unprotectedURLs.stream().
                        noneMatch(uri -> request.getURI().getPath().contains(uri))) {
                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                }
                return request.getBody()
                        .next()
                        .flatMap(dataBuffer -> {
                            String bodyContent = dataBuffer.toString(StandardCharsets.UTF_8);

                            if (bodyContent.isEmpty()) {
                                return onError(exchange, HttpStatus.UNAUTHORIZED);
                            }

                            ObjectMapper objectMapper = new ObjectMapper();
                            try {
                                log.info("Request body -> {}", bodyContent);
                                User user = objectMapper.readValue(bodyContent, User.class);

                                String token = authUtil.getToken(user);

                                if (jwtUtil.isInvalid(token)) {
                                    return onError(exchange, HttpStatus.UNAUTHORIZED);
                                }
                                log.info("Token generated from request body is valid.");
                            } catch (JsonProcessingException e) {
                                log.warn("Invalid JSON body: {}", e.getMessage());
                                return onError(exchange, HttpStatus.BAD_REQUEST);
                            }
                            return chain.filter(exchange);
                        })
                        .switchIfEmpty(onError(exchange, HttpStatus.UNAUTHORIZED));
            }
        } else {

        }

        return chain.filter(exchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

}
