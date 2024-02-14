package com.example.ContaGest.service;

import com.example.ContaGest.model.Role;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class UrlValidatorService {

    public boolean urlNeedToken(String requestURL) {
        List<String> validUrls = Arrays.asList(
                "/api/v1/accountant",
                "/api/v1/accountant/get-invoice",
                "/api/v1/accountant/get-clients",
                "/api/v1/client",
                "/api/v1/img",
                "/api/v1/auth/register-client"
        );
        List<String> validUrlPatterns = List.of(
                "/api/v1/img/\\d+"
        );
        return validUrls.contains(requestURL) || validUrlPatterns.stream()
                .anyMatch(pattern -> Pattern.matches(pattern, requestURL));
    }
    public boolean urlNotExist(String requestURL) {
        List<String> validUrls = Arrays.asList(
                "/api/v1/accountant",
                "/api/v1/accountant/get-invoice",
                "/api/v1/accountant/get-clients",
                "/api/v1/client",
                "/api/v1/img",
                "/api/v1/auth/register-client",
                "/api/v1/auth/register-accountant",
                "/api/v1/auth/login",
                "/api/v1/auth/confirm-accountant",
                "/api/v1/auth/confirm-client",
                "/api/v1/password/confirm-forgot-password",
                "/api/v1/password/password-forgot",
                "/api/v1/auth/confirm-change-email"
        );
        List<String> validUrlPatterns = List.of(
                "/api/v1/img/\\d+"
        );
        return !validUrls.contains(requestURL) && validUrlPatterns.stream()
                .noneMatch(pattern -> Pattern.matches(pattern, requestURL));
    }
    public boolean isAccessAllowed(String userRole, String requestURL) {
        if (userRole.equals(Role.ACCOUNTANT.name())) {
            return requestURL.startsWith("/api/v1/accountant") || requestURL.equals("/api/v1/auth/register-client");
        } else if (userRole.equals(Role.CLIENT.name())) {
            return requestURL.startsWith("/api/v1/client") || requestURL.startsWith("/api/v1/img");
        }
        return false;
    }
}
