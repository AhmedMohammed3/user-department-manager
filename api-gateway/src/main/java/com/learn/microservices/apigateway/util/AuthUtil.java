package com.learn.microservices.apigateway.util;

import com.learn.microservices.apigateway.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AuthUtil {

    private final RestTemplate restTemplate;

    public AuthUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getToken(User user) {
        HttpEntity<User> request = new HttpEntity<>(user);
        ResponseEntity<String> response = restTemplate.exchange("lb://auth-service/auth/login", HttpMethod.POST, request, String.class);
        return response.getBody();
    }
}
