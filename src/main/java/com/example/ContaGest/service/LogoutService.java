package com.example.ContaGest.service;

import com.example.ContaGest.exception.ResourceNotFoundException;
import com.example.ContaGest.repository.TokenRepository;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Service
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final HandlerExceptionResolver exceptionResolver;
    @Autowired
    public LogoutService(TokenRepository tokenRepository, @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver) {
        this.tokenRepository = tokenRepository;
        this.exceptionResolver = exceptionResolver;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new SignatureException("No bearer token found in the request header");
            }
            jwt = authHeader.substring(7);
            var storedToken = tokenRepository.findByToken(jwt).orElseThrow(() -> new ResourceNotFoundException("Token not found"));
            if (storedToken != null) {
                storedToken.setExpired(true);
                storedToken.setRevoke(true);
                tokenRepository.save(storedToken);
            }
        }catch (Exception e){
            exceptionResolver.resolveException(request, response, null, e);
        }
    }
}